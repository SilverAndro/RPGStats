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
import io.github.silverandro.rpgstats.commands.StatsCommand
import io.github.silverandro.rpgstats.event.LevelUpCallback
import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.util.filterInPlace
import io.netty.buffer.Unpooled
import mc.rpgstats.command.CheatCommand
import mc.rpgstats.main.RPGStats
import mc.rpgstats.mixin_logic.OnSneakLogic
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.advancement.Advancement
import net.minecraft.block.*
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.PacketByteBuf
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
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
                    Components.FARMING,
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
                    Components.components.clear()
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
                Components.components.remove(possible)
            } else {
                Components.components[possible] = name
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
                    val preferences = Components.PREFERENCES.get(player)
                    if (preferences.isOptedOutOfButtonSpam && player.isSneaking) {
                        OnSneakLogic.doLogic(true, player)
                    }

                    // Fix stats for respawning players
                    if (RPGStats.needsStatFix.contains(player) && player.isAlive) {
                        Components.components.forEach { (id, _) ->
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
                        val count = Components.components.size
                        val nameData = PacketByteBuf(Unpooled.buffer())
                        val statData = PacketByteBuf(Unpooled.buffer())

                        // How many stats in packet
                        statData.writeInt(count)
                        nameData.writeInt(count)
                        // For each stat
                        for (statId in Components.components.keys) {
                            // Write the stat identifier
                            statData.writeIdentifier(statId)
                            nameData.writeIdentifier(statId)
                            // Write the level and XP
                            statData.writeInt(getComponentLevel(statId, player))
                            statData.writeInt(getComponentXP(statId, player))
                            nameData.writeString(Components.components[statId])
                        }
                        ServerPlayNetworking.send(player, SYNC_STATS_PACKET_ID, statData)
                        ServerPlayNetworking.send(player, SYNC_NAMES_PACKET_ID, nameData)
                    }

                    // Mining lv 50 effect
                    if (player.blockPos.y <= RPGStats.getConfig().toggles.mining.effectLevelTrigger &&
                        getComponentLevel(
                            Components.MINING,
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
                    Components.STATS.sync(player)
                }
                tickCount = 0
            }
        }
    }

    fun registerLevelUpEvents() {
        LevelUpCallback.EVENT.register { player, id, newLevel, hideMessages ->
            // Data driven stats have no actions and won't be registered
            val actions = Components.actions.get(id)
            actions?.forEach {
                it.onLevelUp(player, newLevel, hideMessages)
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
                        addXpAndLevelUp(Components.FARMING, (playerEntity as ServerPlayerEntity), 1)
                    } else {
                        addXpAndLevelUp(Components.FARMING, (playerEntity as ServerPlayerEntity), 1)
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
                    addXpAndLevelUp(Components.MINING, (playerEntity as ServerPlayerEntity), amount)
                }
            }
        }
    }
}