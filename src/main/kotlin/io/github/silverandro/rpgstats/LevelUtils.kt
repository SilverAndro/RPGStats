package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.event.LevelUpCallback
import io.github.silverandro.rpgstats.stats.Components
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

object LevelUtils {
    /**
     * Sets the xp for a stat
     */
    fun setComponentXP(id: Identifier, player: ServerPlayerEntity, newValue: Int) {
        if (RPGStatsMain.config.debug.logRawOps) {
            Constants.debugLogger.info(player.entityName + " xp was set to " + newValue + " in stat " + id.toString())
            Constants.debugLogger.info("Stat is loaded: " + Components.components.containsKey(id))
        }
        if (Components.components.containsKey(id)) {
            Components.STATS.get(player).getOrCreateID(id).xp = newValue
            Components.STATS.sync(player)
        }

        if (getComponentXP(id, player) < 0) {
            setComponentXP(id, player, 0)
        }
    }

    /**
     * Returns the xp for some stat
     */
    fun getComponentXP(id: Identifier, player: ServerPlayerEntity): Int {
        return if (Components.components.containsKey(id)) Components.STATS.get(player)
            .getOrCreateID(id).xp else -1
    }

    /**
     * Sets the level for a stat
     */
    fun setComponentLevel(id: Identifier, player: ServerPlayerEntity, newValue: Int) {
        if (RPGStatsMain.config.debug.logRawOps) {
            Constants.debugLogger.info(player.entityName + " level was set to " + newValue + " in stat " + id.toString())
            Constants.debugLogger.info("Stat is loaded: " + Components.components.containsKey(id))
        }
        if (Components.components.containsKey(id)) {
            Components.STATS.get(player).getOrCreateID(id).level = newValue
            Components.STATS.sync(player)
        }

        if (getComponentLevel(id, player) < 0) {
            setComponentLevel(id, player, 0)
        }
    }

    /**
     * Returns the level of some stat
     */
    fun getComponentLevel(id: Identifier, player: ServerPlayerEntity): Int {
        return if (Components.components.containsKey(id)) Components.STATS.get(player)
            .getOrCreateID(id).level else -1
    }

    /**
     * Calculates the amount of XP needed to level up
     */
    fun calculateXpNeededForLevel(level: Int): Int {
        val config = RPGStatsMain.config
        return if (config.scaling.isCumulative) {
            (1..level).sumOf {
                floor(it.toDouble().pow(config.scaling.power) * config.scaling.scale)
                    .toInt() + config.scaling.base
            }
        } else {
            floor(level.toDouble().pow(config.scaling.power) * config.scaling.scale)
                .toInt() + config.scaling.base
        }
    }

    /**
     * Adds XP to a player and levels them up
     */
    fun addXpAndLevelUp(id: Identifier, player: ServerPlayerEntity, addedXP: Int) {
        if (RPGStatsMain.config.debug.logXpGain) {
            Constants.debugLogger.info(player.entityName + " gained " + addedXP + " xp in stat " + id.toString())
            Constants.debugLogger.info("Stat is loaded: " + Components.components.containsKey(id))
        }
        if (Components.components.containsKey(id)) {
            var nextXP = getComponentXP(id, player) + addedXP
            var currentLevel = getComponentLevel(id, player)
            if (currentLevel < RPGStatsMain.config.scaling.maxLevel) {
                // Enough to level up
                var nextXPForLevelUp = calculateXpNeededForLevel(currentLevel + 1)
                while (nextXP >= nextXPForLevelUp && currentLevel < RPGStatsMain.config.scaling.maxLevel) {
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
                    nextXPForLevelUp = calculateXpNeededForLevel(currentLevel + 1)
                }
                setComponentXP(id, player, nextXP)
                Components.STATS.sync(player)
            }
        }
    }

    /**
     * Returns the display text for a level
     */
    fun getLevelDisplay(id: Identifier, player: ServerPlayerEntity): MutableText {
        val currentLevel = getComponentLevel(id, player)
        val xp = getComponentXP(id, player)
        val name = Components.components[id]
        return if (currentLevel < RPGStatsMain.config.scaling.maxLevel) {
            val nextXP = calculateXpNeededForLevel(currentLevel + 1)
            Text.translatable(name)
                .formatted(Formatting.GOLD)
                .append(
                    Text.translatable("rpgstats.notmaxlevel_trunc", currentLevel, xp, nextXP)
                        .formatted(Formatting.WHITE)
                )
        } else {
            Text.translatable(name)
                .formatted(Formatting.GOLD)
                .append(Text.translatable("rpgstats.maxlevel_trunc", currentLevel).formatted(Formatting.WHITE))
        }
    }

    /**
     * Returns a list of all stat levels
     */
    fun getStatLevelsForPlayer(player: ServerPlayerEntity): List<Int> {
        return Components.components.keys.map { getComponentLevel(it, player) }
    }

    /**
     * Gets the highest level on a player, or 0 if they have no stats
     */
    fun getHighestLevel(player: ServerPlayerEntity): Int {
        val stats = getStatLevelsForPlayer(player)
        return if (stats.isEmpty()) 0 else Collections.max(getStatLevelsForPlayer(player))
    }

    /**
     * Gets the lowest level on a player, or 0 if they have no stats
     */
    fun getLowestLevel(player: ServerPlayerEntity): Int {
        val stats = getStatLevelsForPlayer(player)
        return if (stats.isEmpty()) 0 else Collections.min(getStatLevelsForPlayer(player))
    }

    /**
     * Adds a certain amount of levels to a player, but doesn't touch XP
     */
    fun levelUp(id: Identifier, player: ServerPlayerEntity, amount: Int = 1) {
        val currentLevel = getComponentLevel(id, player)
        val newLevel = min(currentLevel + amount, RPGStatsMain.config.scaling.maxLevel)
        for (i in currentLevel..newLevel) {
            setComponentLevel(id, player, i)
            LevelUpCallback.EVENT.invoker().onLevelUp(player, id, i, false)
        }
        Components.STATS.sync(player)
    }

    /**
     * Removes XP from a player and decrements their level if required
     */
    fun removeXp(id: Identifier, player: ServerPlayerEntity, amount: Int) {
        val currentXp = getComponentXP(id, player)
        setComponentXP(id, player, currentXp - amount)
        while (getComponentXP(id, player) < 0) {
            val currentLevel = getComponentLevel(id, player)
            setComponentLevel(id, player, currentLevel - 1)
            val newXp = getComponentXP(id, player)
            val toLevelUp = newXp + calculateXpNeededForLevel(currentLevel)
            setComponentXP(id, player, toLevelUp)

            if (currentLevel - 1 < 0) {
                setComponentLevel(id, player, 0)
                setComponentXP(id, player, 0)
                break
            }
        }
    }
}