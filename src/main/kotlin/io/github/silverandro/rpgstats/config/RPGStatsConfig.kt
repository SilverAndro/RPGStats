package io.github.silverandro.rpgstats.config

import io.github.silverandro.rpgstats.util.KotlinConfig
import org.quiltmc.config.api.annotations.Comment
import org.quiltmc.config.api.annotations.FloatRange
import org.quiltmc.config.api.annotations.IntegerRange

class RPGStatsConfig : KotlinConfig() {
    @delegate:Comment("If players should lose all stats on death")
    val hardcoreMode by value(false)

    @delegate:Comment("Level scaling formula inputs")
    val scaling by section(LevelScaling())

    @delegate:Comment("Settings for the anticheat implementation")
    val antiCheat by section(AntiCheat())

    @delegate:Comment("Debug logging config")
    val debug by section(Debug())

    class AntiCheat : Section() {
        @delegate:Comment("Prevent duplicate XP from breaking blocks in the same location")
        val blockBreakPos by value(true)

        @delegate:Comment("How many ticks before you gain XP from breaking a block from a location again")
        @delegate:IntegerRange(min = 0, max = Int.MAX_VALUE.toLong())
        val blockBreakDelay by value(5000)
    }

    class LevelScaling : Section() {
        @delegate:FloatRange(min = 0.0001, max = Double.MAX_VALUE)
        val power by value(2.07)

        @delegate:FloatRange(min = 0.0001, max = Double.MAX_VALUE)
        val scale by value(0.52)

        @delegate:IntegerRange(min = 1, max = Int.MAX_VALUE.toLong())
        val base by value(80)

        @delegate:Comment("If the required amount should be the requirements from previous levels combined + new one instead of just solving once")
        val isCumulative by value(false)

        @delegate:Comment("The maximum level allowed")
        @delegate:IntegerRange(min = 0, max = Int.MAX_VALUE.toLong())
        val maxLevel by value(50)
    }

    class Debug : Section() {
        val logXpGain by value(false)
        val logBrokenBlocks by value(false)
        val logRawOps by value(false)
        val logRawWrite by value(false)
        val logAntiCheatPrevention by value(false)
    }
}