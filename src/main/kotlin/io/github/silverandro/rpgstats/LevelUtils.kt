package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.event.LevelUpCallback
import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.main.RPGStats
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*
import kotlin.math.floor
import kotlin.math.pow

object LevelUtils {
    fun setComponentXP(id: Identifier, player: ServerPlayerEntity, newValue: Int) {
        if (RPGStats.getConfig().debug.logRawOps) {
            Constants.debugLogger.info(player.entityName + " xp was set to " + newValue + " in stat " + id.toString())
            Constants.debugLogger.info("Stat is loaded: " + Components.components.containsKey(id))
        }
        if (Components.components.containsKey(id)) {
            Components.STATS.get(player).getOrCreateID(id).xp = newValue
            Components.STATS.sync(player)
        }
    }

    fun getComponentXP(id: Identifier, player: ServerPlayerEntity): Int {
        return if (Components.components.containsKey(id)) Components.STATS.get(player)
            .getOrCreateID(id).xp else -1
    }

    fun setComponentLevel(id: Identifier, player: ServerPlayerEntity, newValue: Int) {
        if (RPGStats.getConfig().debug.logRawOps) {
            Constants.debugLogger.info(player.entityName + " level was set to " + newValue + " in stat " + id.toString())
            Constants.debugLogger.info("Stat is loaded: " + Components.components.containsKey(id))
        }
        if (Components.components.containsKey(id)) {
            Components.STATS.get(player).getOrCreateID(id).level = newValue
            Components.STATS.sync(player)
        }
    }

    fun getComponentLevel(id: Identifier, player: ServerPlayerEntity): Int {
        return if (Components.components.containsKey(id)) Components.STATS.get(player)
            .getOrCreateID(id).level else -1
    }

    fun calculateXpNeededToReachLevel(level: Int): Int {
        val config = RPGStats.getConfig()
        return if (config.scaling.isCumulative) {
            var required = 0
            for (i in 1..level) {
                required += Math.floor(Math.pow(i.toDouble(), config.scaling.power) * config.scaling.scale)
                    .toInt() + config.scaling.base
            }
            required
        } else {
            floor(level.toDouble().pow(config.scaling.power) * config.scaling.scale)
                .toInt() + config.scaling.base
        }
    }

    fun addXpAndLevelUp(id: Identifier, player: ServerPlayerEntity, addedXP: Int) {
        if (RPGStats.getConfig().debug.logXpGain) {
            Constants.debugLogger.info(player.entityName + " gained " + addedXP + " xp in stat " + id.toString())
            Constants.debugLogger.info("Stat is loaded: " + Components.components.containsKey(id))
        }
        if (Components.components.containsKey(id)) {
            var nextXP = getComponentXP(id, player) + addedXP
            var currentLevel = getComponentLevel(id, player)
            if (currentLevel < RPGStats.getConfig().scaling.maxLevel) {
                // Enough to level up
                var nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1)
                while (nextXP >= nextXPForLevelUp && currentLevel < RPGStats.getConfig().scaling.maxLevel) {
                    nextXP -= nextXPForLevelUp
                    currentLevel += 1
                    setComponentLevel(id, player, currentLevel)
                    Components.STATS.sync(player)
                    player.sendMessage(
                        Text.literal("§aRPGStats >§r ")
                            .formatted(Formatting.GREEN)
                            .append(
                                Text.translatable("rpgstats.levelup_1")
                                    .formatted(Formatting.WHITE)
                                    .append(
                                        Text.literal(Components.components[id])
                                            .formatted(Formatting.GOLD)
                                            .append(
                                                Text.translatable("rpgstats.levelup_2")
                                                    .formatted(Formatting.WHITE)
                                                    .append(
                                                        Text.literal(getComponentLevel(id, player).toString())
                                                            .formatted(Formatting.GOLD)
                                                    )
                                            )
                                    )
                            ), false
                    )
                    LevelUpCallback.EVENT.invoker().onLevelUp(player, id, currentLevel, false)
                    nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1)
                }
                setComponentXP(id, player, nextXP)
                Components.STATS.sync(player)
            }
        }
    }

    fun getFormattedLevelData(id: Identifier, player: ServerPlayerEntity): MutableText? {
        val currentLevel = getComponentLevel(id, player)
        val xp = getComponentXP(id, player)
        val name = Components.components[id]
        val capped = name!!.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1)
        return if (currentLevel < RPGStats.getConfig().scaling.maxLevel) {
            val nextXP = calculateXpNeededToReachLevel(currentLevel + 1)
            Text.literal(capped)
                .formatted(Formatting.GOLD)
                .append(
                    Text.translatable("rpgstats.notmaxlevel_trunc", currentLevel, xp, nextXP)
                        .formatted(Formatting.WHITE)
                )
        } else {
            Text.literal(capped)
                .formatted(Formatting.GOLD)
                .append(Text.translatable("rpgstats.maxlevel_trunc", currentLevel).formatted(Formatting.WHITE))
        }
    }

    fun getNotFormattedLevelData(id: Identifier, player: ServerPlayerEntity): Text? {
        val currentLevel = getComponentLevel(id, player)
        val xp = getComponentXP(id, player)
        val name = Components.components[id]
        val capped = name!!.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1)
        return if (currentLevel < RPGStats.getConfig().scaling.maxLevel) {
            val nextXP = calculateXpNeededToReachLevel(currentLevel + 1)
            Text.translatable("rpgstats.notmaxlevel", capped, currentLevel, xp, nextXP)
        } else {
            Text.translatable("rpgstats.maxlevel", capped, currentLevel)
        }
    }

    fun getStatLevelsForPlayer(player: ServerPlayerEntity): ArrayList<Int> {
        val result = ArrayList<Int>()
        for (stat in Components.components.keys) {
            result.add(getComponentLevel(stat, player))
        }
        return result
    }

    fun getHighestLevel(player: ServerPlayerEntity): Int {
        val stats = getStatLevelsForPlayer(player)
        return if (stats.isEmpty()) 0 else Collections.max(getStatLevelsForPlayer(player))
    }

    fun getLowestLevel(player: ServerPlayerEntity): Int {
        val stats = getStatLevelsForPlayer(player)
        return if (stats.isEmpty()) 0 else Collections.min(getStatLevelsForPlayer(player))
    }

    fun softLevelUp(id: Identifier, player: ServerPlayerEntity) {
        val currentLevel = getComponentLevel(id, player)
        val savedLevel = if (currentLevel > RPGStats.getConfig().scaling.maxLevel) {
            setComponentLevel(id, player, RPGStats.getConfig().scaling.maxLevel)
            setComponentXP(id, player, 0)
            RPGStats.getConfig().scaling.maxLevel
        } else currentLevel

        for (i in 1..savedLevel) {
            setComponentLevel(id, player, i)
            LevelUpCallback.EVENT.invoker().onLevelUp(player, id, i, true)
        }

        Components.STATS.sync(player)
    }
}