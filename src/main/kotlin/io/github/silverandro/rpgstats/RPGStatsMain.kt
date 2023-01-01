package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.advancemnents.LevelUpCriterion
import io.github.silverandro.rpgstats.datadrive.stats.StatsManager
import io.github.silverandro.rpgstats.datadrive.xp.XpData
import io.github.silverandro.rpgstats.hooky.Hooky
import io.github.silverandro.rpgstats.mixin.accessor.CriteriaAccessor
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.config.QuiltConfig
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer


object RPGStatsMain : ModInitializer {
    @JvmField
    val config = QuiltConfig.create(
        "rpgstats",
        "main",
        RPGStatsConfig::class.java
    )

    @JvmField
    val damageBlacklist = QuiltConfig.create(
        "rpgstats",
        "damage_blacklist",
        RPGStatsDamageBlacklist::class.java
    )

    @JvmField
    val levelConfig = QuiltConfig.create(
        "rpgstats",
        "level_abilities",
        RPGStatsLevelConfig::class.java
    )

    val levelUpCriterion = LevelUpCriterion()

    override fun onInitialize(mod: ModContainer) {
        Constants.LOG.info("Hello from ${mod.metadata().name()}")
        // Criterion
        CriteriaAccessor.getValues()[LevelUpCriterion.ID] = levelUpCriterion

        // Events
        Hooky.registerAll()
        StatsManager.register()
        XpData.poke()
        Events.registerLevelUpEvents()
    }
}