package io.github.silverandro.rpgstats.stats

import io.github.silverandro.rpgstats.Constants.debugLogger
import mc.rpgstats.main.RPGStats
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

class StatEntry(
    val id: Identifier,
    level: Int = 0,
    xp: Int = 0
) {
    val translationKey = "${id.namespace}.stat.${id.path.replace("/", "_")}"

    var level: Int = 0
        set(value) {
            if (RPGStats.getConfig().debug.logRawWrite) {
                debugLogger.info("Im $id and my level is now $level")
            }
            field = value
        }
    var xp: Int = 0
        set(value) {
            if (RPGStats.getConfig().debug.logRawWrite) {
                debugLogger.info("Im $id and my xp is now $xp")
            }
            field = value
        }

    init {
        this.level = level
        this.xp = xp
    }

    fun addToCompound(nbt: NbtCompound) {
        nbt.apply {
            put(id.toString(), NbtCompound().apply {
                putInt("level", level)
                putInt("xp", xp)
            })
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StatEntry

        if (id != other.id) return false
        if (level != other.level) return false
        if (xp != other.xp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + level
        result = 31 * result + xp
        return result
    }
}
