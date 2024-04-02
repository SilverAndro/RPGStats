/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.advancemnents.LevelUpCriterion
import io.github.silverandro.rpgstats.config.RPGStatsConfig
import io.github.silverandro.rpgstats.config.RPGStatsLevelConfig
import io.github.silverandro.rpgstats.datadrive.stats.StatsManager
import io.github.silverandro.rpgstats.datadrive.xp.XpData
import io.github.silverandro.rpgstats.hooky.Hooky
import net.minecraft.advancement.criterion.Criteria
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.config.v2.QuiltConfig
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer


object RPGStatsMain : ModInitializer {
    @JvmField
    val config = RPGStatsConfig().also {
        QuiltConfig.create(
            "rpgstats",
            "main",
            it
        )
    }

    @JvmField
    val levelConfig = RPGStatsLevelConfig().also {
        QuiltConfig.create(
            "rpgstats",
            "level_abilities",
            it
        )
    }

    val levelUpCriterion = LevelUpCriterion()

    override fun onInitialize(mod: ModContainer) {
        Constants.LOG.info("Hello from ${mod.metadata().name()}!")
        // Criterion
        Criteria.register("${Constants.MOD_ID}:player_level", levelUpCriterion)

        // Events
        XpData.poke()
        Hooky.registerAll()
        StatsManager.register()
        Events.registerLevelUpEvents()
    }
}