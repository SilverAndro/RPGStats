package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.Constants.LEVELS_MAX
import io.github.silverandro.rpgstats.Constants.SYNC_NAMES_PACKET_ID
import io.github.silverandro.rpgstats.Constants.SYNC_STATS_PACKET_ID
import io.github.silverandro.rpgstats.Constants.debugLogger
import io.github.silverandro.rpgstats.LevelUtils.addXpAndLevelUp
import io.github.silverandro.rpgstats.LevelUtils.getComponentLevel
import io.github.silverandro.rpgstats.LevelUtils.getComponentXP
import io.github.silverandro.rpgstats.LevelUtils.getLowestLevel
import io.github.silverandro.rpgstats.LevelUtils.softLevelUp
import io.github.silverandro.rpgstats.event.LevelUpCallback
import io.github.silverandro.rpgstats.util.filterInPlace
import io.netty.buffer.Unpooled
import mc.rpgstats.command.CheatCommand
import mc.rpgstats.command.StatsCommand
import mc.rpgstats.main.CustomComponents
import mc.rpgstats.main.RPGStats
import mc.rpgstats.mixin_logic.OnSneakLogic
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.advancement.Advancement
import net.minecraft.block.*
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.PacketByteBuf
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import org.quiltmc.qsl.command.api.CommandRegistrationCallback
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents
import org.quiltmc.qsl.networking.api.PlayerLookup
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import wraith.harvest_scythes.api.scythe.HSScythesEvents
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Events {
    private var tickCount = 0
    private val blacklistedPos = ConcurrentHashMap<BlockPos, Int>()

    fun registerCommandRegisters() {
        // Commands
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            StatsCommand.register(dispatcher)
            CheatCommand.register(dispatcher)
        }
    }

    fun registerHSCompat() {
        HSScythesEvents.addHarvestListener { harvestEvent ->
            val user = harvestEvent.user
            if (user is ServerPlayerEntity) {
                addXpAndLevelUp(
                    CustomComponents.FARMING,
                    user,
                    harvestEvent.totalBlocksHarvested()
                )
            }
        }
    }

    fun registerResourceReloadListeners() {
        // Data driven stuff
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
            .registerReloadListener(object : SimpleSynchronousResourceReloadListener {
                override fun getFabricId(): Identifier {
                    return Identifier("rpgstats:stats")
                }

                override fun reload(manager: ResourceManager) {
                    CustomComponents.components.clear()
                    println("RPGStats reload!")
                    manager.findAllResources("rpgstats") { true }.forEach { _, list ->
                        list.forEach { resource ->
                            resource.openBufferedReader().use {
                                handleLines(it.lines().toList().toTypedArray())
                            }
                        }
                    }
                }
            })
    }

    private fun handleLines(text: Array<String>) {
        text.forEach {
            val (id, name) = it
                .replace("\r", "")
                .split(">".toRegex())
                .dropLastWhile { it.isBlank() }
                .toTypedArray()
            val possible =
                Identifier.tryParse(id) ?: Identifier.tryParse(id.substring(1)) ?: throw IllegalArgumentException(
                    "Could not parse lines in rpgstats stat loading, got $id and $name before error"
                )

            if (id.startsWith("-")) {
                CustomComponents.components.remove(possible)
            } else {
                CustomComponents.components[possible] = name
            }
        }
    }

    fun registerServerTickEvents() {
        // Syncing and advancements
        ServerTickEvents.END.register { server ->
            blacklistedPos.filterInPlace { blockPos, i ->
                blacklistedPos[blockPos] = i - 1
                i <= 0
            }

            if (tickCount++ >= 20) {
                val advancements = server.advancementLoader.advancements
                PlayerLookup.all(server).forEach { player ->
                    // Do sneak logic if holding sneak and opted out of spam
                    val preferences = CustomComponents.PREFERENCES.get(player)
                    if (preferences.isOptedOutOfButtonSpam && player.isSneaking) {
                        OnSneakLogic.doLogic(true, player)
                    }

                    // Fix stats for respawning players
                    if (RPGStats.needsStatFix.contains(player) && player.isAlive) {
                        CustomComponents.components.forEach { (id, _) ->
                            softLevelUp(id, player)
                        }
                        RPGStats.needsStatFix.remove(player)
                    }

                    // Grant the hidden max level advancement
                    val possible = advancements
                        .stream()
                        .filter { advancement: Advancement -> advancement.id == LEVELS_MAX }
                        .findFirst()
                    if (possible.isPresent) {
                        if (!player.advancementTracker.getProgress(possible.get()).isDone) {
                            if (getLowestLevel(player) >= 50) {
                                player.advancementTracker.grantCriterion(possible.get(), "trigger")
                            }
                        }
                    }

                    // Client has the mod installed
                    if (ServerPlayNetworking.canSend(player, SYNC_NAMES_PACKET_ID)) {
                        val count = CustomComponents.components.size
                        val nameData = PacketByteBuf(Unpooled.buffer())
                        val statData = PacketByteBuf(Unpooled.buffer())

                        // How many stats in packet
                        statData.writeInt(count)
                        nameData.writeInt(count)
                        // For each stat
                        for (statId in CustomComponents.components.keys) {
                            // Write the stat identifier
                            statData.writeIdentifier(statId)
                            nameData.writeIdentifier(statId)
                            // Write the level and XP
                            statData.writeInt(getComponentLevel(statId, player))
                            statData.writeInt(getComponentXP(statId!!, player))
                            nameData.writeString(CustomComponents.components[statId])
                        }
                        ServerPlayNetworking.send(player, SYNC_STATS_PACKET_ID, statData)
                        ServerPlayNetworking.send(player, SYNC_NAMES_PACKET_ID, nameData)
                    }

                    // Mining lv 50 effect
                    if (player.blockPos.y <= RPGStats.getConfig().toggles.mining.effectLevelTrigger &&
                        getComponentLevel(
                            CustomComponents.MINING,
                            player
                        ) >= 50 && RPGStats.getConfig().toggles.mining.enableLv50Buff
                    ) {
                        player.addStatusEffect(
                            StatusEffectInstance(
                                StatusEffects.NIGHT_VISION,
                                13 * 20,
                                0,
                                true,
                                false,
                                true
                            )
                        )
                    }
                    CustomComponents.STATS.sync(player)
                }
                tickCount = 0
            }
        }
    }

    fun registerLevelUpEvents() {
        LevelUpCallback.EVENT.register { player, id, newLevel, hideMessages ->
            if (id == CustomComponents.DEFENSE) {
                player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)!!.baseValue =
                    player.getAttributeBaseValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) + 0.01
                if (!hideMessages) player.sendMessage(Text.literal("§a+0.01§r Knockback resistance"), false)
                if (newLevel % RPGStats.getConfig().defenseHP.everyXLevels == 0 && newLevel > RPGStats.getConfig().defenseHP.afterLevel) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)!!.baseValue =
                        player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) + RPGStats.getConfig().defenseHP.addAmount
                    if (!hideMessages) player.sendMessage(Text.literal("§a+1§r Health"), false)
                }
                if (!hideMessages) {
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aNimble§r - 5% chance to avoid damage"), false)
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aNimble II§r - 10% chance to avoid damage"), false)
                    }
                }
            }
        }

        LevelUpCallback.EVENT.register { player, id, newLevel, hideMessages ->
            if (id == CustomComponents.FARMING) {
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+1§r Bonemeal efficiency"), false)
                    if (newLevel == 25) {
                        player.sendMessage(
                            Text.literal("§aNurturing§r - Shift rapidly to grow nearby crops (while holding hoe)"),
                            false
                        )
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aNurturing II§r - Nurturing has increased range"), false)
                    }
                }
            }
        }

        LevelUpCallback.EVENT.register { player, id, newLevel, hideMessages ->
            if (id == CustomComponents.FISHING) {
                player.getAttributeInstance(EntityAttributes.GENERIC_LUCK)!!.baseValue =
                    player.getAttributeBaseValue(EntityAttributes.GENERIC_LUCK) + 0.05
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+0.05§r Luck"), false)
                    if (newLevel == 25) {
                        player.sendMessage(
                            Text.literal("§aVitamin rich§r - Eating fish grants you a temporary positive effect"),
                            false
                        )
                    } else if (newLevel == 50) {
                        player.sendMessage(
                            Text.literal("§aTeach a man to fish§r - Extra saturation when eating"),
                            false
                        )
                    }
                }
            }
        }

        LevelUpCallback.EVENT.register { player, id, newLevel, hideMessages ->
            if (id == CustomComponents.MAGIC) {
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+1§r Drunk potion duration"), false)
                    if (newLevel % 3 == 0) {
                        player.sendMessage(Text.literal("§a+1§r Potion drink speed"), false)
                    }
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aVax§r - Immune to poison"), false)
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aDead inside§r - Immune to wither"), false)
                    }
                }
            }
        }

        LevelUpCallback.EVENT.register { player, id, newLevel, hideMessages ->
            if (id == CustomComponents.MELEE) {
                player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)!!.baseValue =
                    player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + RPGStats.getConfig().melee.attackDamagePerLevel
                if (!hideMessages) {
                    player.sendMessage(
                        Text.literal("§a+" + RPGStats.getConfig().melee.attackDamagePerLevel + "§r Melee damage"),
                        false
                    )
                    if (newLevel == 25) {
                        player.sendMessage(
                            Text.literal("§aBloodthirst§r - Regain 1 heart after killing a monster"),
                            false
                        )
                    } else if (newLevel == 50) {
                        player.sendMessage(
                            Text.literal("§aBloodthirst II§r - Regain 2 hearts after killing a monster"),
                            false
                        )
                    }
                }
            }
        }

        LevelUpCallback.EVENT.register { player, id, newLevel, hideMessages ->
            if (id == CustomComponents.MINING) {
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+0.1§r Additional Mining Speed"), false)
                    if (newLevel == 25) {
                        player.sendMessage(
                            Text.literal("§aMagically infused§r - Extra 5% chance to not consume durability with unbreaking."),
                            false
                        )
                    } else if (newLevel == 50) {
                        player.sendMessage(
                            Text.literal("§aMiners sight§r - Night vision below y" + RPGStats.getConfig().toggles.mining.effectLevelTrigger),
                            false
                        )
                    }
                }
            }
        }

        LevelUpCallback.EVENT.register { player, id, newLevel, hideMessages ->
            if (id == CustomComponents.RANGED) {
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+1§r Bow accuracy"), false)
                    if (newLevel == 25) {
                        player.sendMessage(
                            Text.literal("§aAqueus§r - Impaling applies to all mobs, not just water based ones"),
                            false
                        )
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aNix§r - You no longer need arrows"), false)
                    }
                }
            }
        }
    }

    fun registerBlockBreakListeners() {
        PlayerBlockBreakEvents.AFTER.register { world, playerEntity, blockPos, blockState, _ ->
            if (!world.isClient) {
                if (RPGStats.getConfig().antiCheat.blockBreakPos) {
                    if (blacklistedPos.containsKey(blockPos)) {
                        if (RPGStats.getConfig().debug.logAntiCheatPrevention) {
                            debugLogger.info("Ignoring block break at $blockPos because it was previously broken")
                        }
                        return@register
                    } else {
                        blacklistedPos[blockPos] = RPGStats.getConfig().antiCheat.blockBreakDelay
                    }
                }

                val block = blockState.block
                if (RPGStats.getConfig().debug.logBrokenBlocks) {
                    debugLogger.info(playerEntity.entityName + " broke " + block.translationKey + " at " + blockPos)
                }

                if (block is CropBlock || block is PumpkinBlock || block is MelonBlock || block is CocoaBlock) {
                    if (block is CropBlock && block.isMature(blockState)) {
                        addXpAndLevelUp(CustomComponents.FARMING, (playerEntity as ServerPlayerEntity), 1)
                    } else {
                        addXpAndLevelUp(CustomComponents.FARMING, (playerEntity as ServerPlayerEntity), 1)
                    }
                }

                val random = Random()
                if ((block === Blocks.ANCIENT_DEBRIS || Registry.BLOCK.getId(block).path.contains("ore")) && random.nextBoolean()) {
                    val amount =
                        if (block === Blocks.COAL_ORE || block === Blocks.NETHER_GOLD_ORE || block === Blocks.DEEPSLATE_COAL_ORE) {
                            1
                        } else if (block === Blocks.IRON_ORE || block === Blocks.NETHER_QUARTZ_ORE || block === Blocks.DEEPSLATE_IRON_ORE || block === Blocks.COPPER_ORE || block === Blocks.DEEPSLATE_COPPER_ORE) {
                            2
                        } else if (block === Blocks.GOLD_ORE || block === Blocks.LAPIS_ORE || block === Blocks.REDSTONE_ORE || block === Blocks.DEEPSLATE_GOLD_ORE || block === Blocks.DEEPSLATE_LAPIS_ORE || block === Blocks.DEEPSLATE_REDSTONE_ORE) {
                            3
                        } else if (block === Blocks.EMERALD_ORE || block === Blocks.DEEPSLATE_EMERALD_ORE) {
                            4
                        } else if (block === Blocks.DIAMOND_ORE || block === Blocks.ANCIENT_DEBRIS || block === Blocks.DEEPSLATE_DIAMOND_ORE) {
                            5
                        } else {
                            2
                        }
                    addXpAndLevelUp(CustomComponents.MINING, (playerEntity as ServerPlayerEntity), amount)
                }
            }
        }
    }
}