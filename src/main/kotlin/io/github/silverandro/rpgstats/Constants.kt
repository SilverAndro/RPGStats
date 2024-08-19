/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats

import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Constants {
    const val MOD_ID = "rpgstats"
    val SYNC_STATS_PACKET_ID = Identifier.of(MOD_ID, "sync_stats")
    val SYNC_NAMES_PACKET_ID = Identifier.of(MOD_ID, "sync_names")
    val OPEN_GUI = Identifier.of(MOD_ID, "open_gui")

    val LOG: Logger = LogManager.getLogger("RPGStats")
    val debugLogger: Logger = LogManager.getLogger("RPGStats Debug")

    val LEVELS_MAX = Identifier.of(MOD_ID, "levels_max")
    val ANY_ID = Identifier.of(MOD_ID, "_any")

    val LEVEL_SELECTOR_ID = Identifier.of(MOD_ID, "rpgstats_levels")
    val XP_SELECTOR_ID = Identifier.of(MOD_ID, "rpgstats_xp")

    val json = Json {
        if (FabricLoader.getInstance().isDevelopmentEnvironment.not()) {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}