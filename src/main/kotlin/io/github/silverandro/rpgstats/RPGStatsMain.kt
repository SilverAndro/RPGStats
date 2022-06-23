package io.github.silverandro.rpgstats

import mc.rpgstats.main.RPGStats
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

object RPGStatsMain : ModInitializer {
    override fun onInitialize(mod: ModContainer) {
        println("Hello from ${mod.metadata().name()}")
        RPGStats().onInitialize()
    }
}