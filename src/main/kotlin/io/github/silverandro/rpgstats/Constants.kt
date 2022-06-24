package io.github.silverandro.rpgstats

import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Constants {
    const val MOD_ID = "rpgstats"
    val SYNC_STATS_PACKET_ID = Identifier(MOD_ID, "sync_stats")
    val SYNC_NAMES_PACKET_ID = Identifier(MOD_ID, "sync_names")
    val OPEN_GUI = Identifier(MOD_ID, "open_gui")

    val debugLogger: Logger = LogManager.getLogger("RPGStats Debug")

    val LEVELS_MAX = Identifier(MOD_ID, "levels_max")
}