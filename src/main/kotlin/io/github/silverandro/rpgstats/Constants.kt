package io.github.silverandro.rpgstats

import kotlinx.serialization.json.Json
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.quiltmc.loader.api.QuiltLoader

object Constants {
    const val MOD_ID = "rpgstats"
    val SYNC_STATS_PACKET_ID = Identifier(MOD_ID, "sync_stats")
    val SYNC_NAMES_PACKET_ID = Identifier(MOD_ID, "sync_names")
    val OPEN_GUI = Identifier(MOD_ID, "open_gui")

    val LOG: Logger = LogManager.getLogger("RPGStats")
    val debugLogger: Logger = LogManager.getLogger("RPGStats Debug")

    val LEVELS_MAX = Identifier(MOD_ID, "levels_max")

    val json = Json {
        if (QuiltLoader.isDevelopmentEnvironment().not()) {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}