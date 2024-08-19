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
import io.github.silverandro.rpgstats.datadrive.xp.XPLoader
import io.github.silverandro.rpgstats.hooky.Hooky
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.api.RegisterType
import net.fabricmc.api.ModInitializer
import net.minecraft.advancement.criterion.Criteria


object RPGStatsMain : ModInitializer {
    @JvmField
    val config = ConfigApi.registerAndLoadConfig({ RPGStatsConfig() }, RegisterType.SERVER)

    @JvmField
    val levelConfig = ConfigApi.registerAndLoadConfig({ RPGStatsLevelConfig() }, RegisterType.SERVER)

    val levelUpCriterion = LevelUpCriterion()

    override fun onInitialize() {
        Constants.LOG.info("Hello from RPGStats!")
        // Criterion
        Criteria.register("${Constants.MOD_ID}:player_level", levelUpCriterion)

        // Events
        Hooky.registerAll()
        StatsManager.register()
        XPLoader.register()
        Events.registerLevelUpEvents()
    }
}