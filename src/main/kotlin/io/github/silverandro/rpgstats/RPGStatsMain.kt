package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.advancemnents.LevelUpCriterion
import io.github.silverandro.rpgstats.mixin.accessor.CriteriaAccessor
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.config.QuiltConfig
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.spongepowered.asm.mixin.MixinEnvironment


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
        Events.registerCommandRegisters()
        Events.registerResourceReloadListeners()
        Events.registerServerTickEvents()
        Events.registerLevelUpEvents()
        Events.registerBlockBreakListeners()

        // Harvest scythes event
        if (QuiltLoader.isModLoaded("harvest_scythes")) {
            Events.registerHSCompat()
        }

        if (QuiltLoader.isDevelopmentEnvironment()) {
            // Audit mixins for issues if in dev
            MixinEnvironment.getCurrentEnvironment().audit()
        }
    }
}