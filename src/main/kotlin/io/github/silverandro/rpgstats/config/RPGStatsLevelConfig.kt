package io.github.silverandro.rpgstats.config

import io.github.silverandro.rpgstats.Constants
import me.fzzyhmstrs.fzzy_config.annotations.Comment
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.util.Walkable
import net.minecraft.util.Identifier

class RPGStatsLevelConfig : Config(Identifier.of(Constants.MOD_ID, "level_abilities")) {
    val magic = LevelBuffToggles()
    val melee = MeleeBuffToggles()
    val fishing = LevelBuffToggles()
    val ranged = LevelBuffToggles()
    val defense = DefenseBuffToggles()
    val mining = MiningBuffToggles()
    val farming = LevelBuffToggles()

    open class LevelBuffToggles : Walkable {
        val enableLv25Buff = true
        val enableLv50Buff = true
    }

    class MiningBuffToggles : LevelBuffToggles() {
        @Comment("At what Y level does the lv50 effect trigger?")
        val effectLevelTrigger = 20
    }

    class MeleeBuffToggles : LevelBuffToggles() {
        @Comment("How much attack damage is gained per level")
        val attackDamagePerLevel = 0.08
    }

    class DefenseBuffToggles : LevelBuffToggles() {
        @Comment("Will only grant HP every X levels")
        val everyXLevels = 2

        @Comment("How much HP to grant on trigger")
        val addAmount = 1

        @Comment("Minimum level before you start getting HP (Exclusive)")
        val afterLevel = 10
    }
}