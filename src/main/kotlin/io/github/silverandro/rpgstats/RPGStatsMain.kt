package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.advancemnents.LevelUpCriterion
import io.github.silverandro.rpgstats.main.RPGStats
import io.github.silverandro.rpgstats.main.RPGStatsConfig
import io.github.silverandro.rpgstats.mixin.accessor.CriteriaAccessor
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.spongepowered.asm.mixin.MixinEnvironment

object RPGStatsMain : ModInitializer {
    override fun onInitialize(mod: ModContainer) {
        println("Hello from ${mod.metadata().name()}")
        // Criterion
        CriteriaAccessor.getValues()[LevelUpCriterion.ID] = RPGStats.levelUpCriterion

        // Config
        AutoConfig.register(
            RPGStatsConfig::class.java
        ) { definition: Config, configClass: Class<RPGStatsConfig> ->
            JanksonConfigSerializer(
                definition,
                configClass
            )
        }

        // Events
        Events.registerCommandRegisters()
        Events.registerResourceReloadListeners()
        Events.registerServerTickEvents()
        Events.registerLevelUpEvents()
        Events.registerBlockBreakListeners()

        if (QuiltLoader.isModLoaded("harvest_scythes")) {
            Events.registerHSCompat()
        }

        if (QuiltLoader.isDevelopmentEnvironment()) {
            // Audit mixins for issues
            MixinEnvironment.getCurrentEnvironment().audit()
        }
    }
}