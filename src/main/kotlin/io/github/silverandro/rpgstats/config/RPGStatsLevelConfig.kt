package io.github.silverandro.rpgstats.config

import io.github.silverandro.rpgstats.util.KotlinConfig
import org.quiltmc.config.api.annotations.Comment

class RPGStatsLevelConfig : KotlinConfig() {
    val magic by section(LevelBuffToggles())
    val melee by section(MeleeBuffToggles())
    val fishing by section(LevelBuffToggles())
    val ranged by section(LevelBuffToggles())
    val defense by section(DefenseBuffToggles())
    val mining by section(MiningBuffToggles())
    val farming by section(LevelBuffToggles())

    open class LevelBuffToggles : Section() {
        val enableLv25Buff by value(true)
        val enableLv50Buff by value(true)
    }

    class MiningBuffToggles : LevelBuffToggles() {
        @delegate:Comment("At what Y level does the lv50 effect trigger?")
        val effectLevelTrigger by value(20)
    }

    class MeleeBuffToggles : LevelBuffToggles() {
        @delegate:Comment("How much attack damage is gained per level")
        val attackDamagePerLevel by value(0.08)
    }

    class DefenseBuffToggles : LevelBuffToggles() {
        @delegate:Comment("Will only grant HP every X levels")
        val everyXLevels by value(2)

        @delegate:Comment("How much HP to grant on trigger")
        val addAmount by value(1)

        @delegate:Comment("Minimum level before you start getting HP (Exclusive)")
        val afterLevel by value(10)
    }
}