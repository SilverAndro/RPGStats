package io.github.silverandro.rpgstats

//import io.github.silverandro.rpgstats.commands.CheatCommand
import io.github.silverandro.rpgstats.Constants.LEVELS_MAX
import io.github.silverandro.rpgstats.Constants.SYNC_NAMES_PACKET_ID
import io.github.silverandro.rpgstats.Constants.SYNC_STATS_PACKET_ID
import io.github.silverandro.rpgstats.Constants.debugLogger
import io.github.silverandro.rpgstats.LevelUtils.addXpAndLevelUp
import io.github.silverandro.rpgstats.LevelUtils.getComponentLevel
import io.github.silverandro.rpgstats.LevelUtils.getComponentXP
import io.github.silverandro.rpgstats.LevelUtils.getLowestLevel
import io.github.silverandro.rpgstats.LevelUtils.softLevelUp
import io.github.silverandro.rpgstats.command.CheatCommand
import io.github.silverandro.rpgstats.commands.StatsCommand
import io.github.silverandro.rpgstats.datadrive.stats.StatsManager
import io.github.silverandro.rpgstats.datadrive.xp.XpData
import io.github.silverandro.rpgstats.event.LevelUpCallback
import io.github.silverandro.rpgstats.mixin_logic.OnSneakLogic
import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.stats.systems.StatAttributeAction
import io.github.silverandro.rpgstats.util.filterInPlace
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.advancement.Advancement
import net.minecraft.block.CropBlock
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import org.quiltmc.qkl.wrapper.qsl.networking.allPlayers
import org.quiltmc.qsl.command.api.CommandRegistrationCallback
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents
import org.quiltmc.qsl.networking.api.PlayerLookup
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import wraith.harvest_scythes.api.scythe.HSScythesEvents
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Events {
    @JvmField
    val needsStatFix = mutableListOf<ServerPlayerEntity>()

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
        StatsManager.register()
        XpData.poke()
    }

    fun registerServerTickEvents() {
        // Syncing and advancements
        ServerTickEvents.END.register { server ->
            blacklistedPos.filterInPlace { blockPos, i ->
                blacklistedPos[blockPos] = i - 1
                i <= 0
            }

            if (tickCount++ >= 200) {
                val advancements = server.advancementLoader.advancements
                PlayerLookup.all(server).forEach { player ->
                    // Do sneak logic if holding sneak and opted out of spam
                    val preferences = Components.PREFERENCES.get(player)
                    if (preferences.isOptedOutOfButtonSpam && player.isSneaking) {
                        OnSneakLogic.doLogic(true, player)
                    }

                    // Fix stats for respawning players
                    if (needsStatFix.contains(player) && player.isAlive) {
                        Components.components.forEach { (id, _) ->
                            softLevelUp(id, player)
                        }
                    }
                    needsStatFix.filterInPlace { it.isAlive }

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
                    if (player.blockPos.y <= RPGStatsMain.levelConfig.mining.effectLevelTrigger &&
                        getComponentLevel(
                            Components.MINING,
                            player
                        ) >= 50 && RPGStatsMain.levelConfig.mining.enableLv50Buff
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

            // For every player
            server.allPlayers.forEach { player ->
                // Get all their stats/components
                Components.components.keys.forEach { id ->
                    // Map those into actions
                    Components.actions.get(id)?.forEachIndexed { actionIndex, action ->
                        // If the action modifies an attribute
                        if (action is StatAttributeAction) {
                            // Compute the total modification
                            var total = 0.0
                            for (x in 1..getComponentLevel(id, player)) {
                                if (action.shouldApply(x)) {
                                    total += action.value
                                }
                            }

                            // Generate a modifier based on the total amount for this player
                            val modifier = EntityAttributeModifier(
                                Components.modifierIDFor(id.toUnderscoreSeparatedString(), actionIndex),
                                "$id ${action.stat.translationKey}",
                                total,
                                EntityAttributeModifier.Operation.ADDITION
                            )

                            // If the player has an attribute that is modified by this modifier
                            with (player.getAttributeInstance(action.stat) ?: return@forEachIndexed) {
                                // Remove it if we already applied this action
                                if (hasModifier(modifier)) {
                                    removeModifier(modifier)
                                }
                                // Apply modifier in a way that won't save
                                addTemporaryModifier(modifier)
                            }
                        }
                    }
                }
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
                if (RPGStatsMain.config.antiCheat.blockBreakPos) {
                    if (blacklistedPos.containsKey(blockPos)) {
                        if (RPGStatsMain.config.debug.logAntiCheatPrevention) {
                            debugLogger.info("Ignoring block break at $blockPos because it was previously broken")
                        }
                        return@register
                    } else {
                        blacklistedPos[blockPos] = RPGStatsMain.config.antiCheat.blockBreakDelay
                    }
                }

                val block = blockState.block
                if (RPGStatsMain.config.debug.logBrokenBlocks) {
                    debugLogger.info(playerEntity.entityName + " broke " + block.translationKey + " at " + blockPos)
                }

                val player = playerEntity as ServerPlayerEntity
                if (Random().nextBoolean()) {
                    if (block is CropBlock && !block.isMature(blockState)) return@register

                    val amount = XpData.BLOCK_XP.get(block).orElse(null) ?: return@register
                    amount.ifLeft {
                        addXpAndLevelUp(it.id, player, it.amount)
                    }.ifRight {
                        it.forEach {
                            addXpAndLevelUp(it.id, player, it.amount)
                        }
                    }
                }
            }
        }
    }
}