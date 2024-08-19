package io.github.silverandro.rpgstats.config

import io.github.silverandro.rpgstats.Constants
import me.fzzyhmstrs.fzzy_config.annotations.Comment
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.util.Walkable
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import net.minecraft.util.Identifier

class RPGStatsConfig : Config(Identifier.of(Constants.MOD_ID, "main")) {
    @Comment("If players should lose all stats on death")
    val hardcoreMode = false

    @Comment("Level scaling formula inputs")
    val scaling = LevelScaling()

    @Comment("Settings for the anticheat implementation")
    val antiCheat = AntiCheat()

    @Comment("Debug logging config")
    val debug = Debug()

    class AntiCheat : Walkable {
        @Comment("Prevent duplicate XP from breaking blocks in the same location")
        val blockBreakPos = true

        @Comment("How many ticks before you gain XP from breaking a block from a location again")
        @ValidatedInt.Restrict(0)
        val blockBreakDelay = 6000
    }

    class LevelScaling : Walkable {
        @ValidatedDouble.Restrict(0.0001)
        val power = 2.07

        @ValidatedDouble.Restrict(0.0001)
        val scale = 0.52

        @ValidatedInt.Restrict(1)
        val base = 80

        @Comment("If the required amount should be the requirements from previous levels combined + new one instead of just solving once")
        val isCumulative = false

        @Comment("The maximum level allowed")
        @ValidatedInt.Restrict(0)
        val maxLevel = 50
    }

    class Debug : ConfigSection() {
        val logXpGain = false
        val logBrokenBlocks = false
        val logRawOps = false
        val logRawWrite = false
        val logAntiCheatPrevention = false
    }
}