package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.stats.internal.XpBarLocation
import io.github.silverandro.rpgstats.stats.internal.XpBarShow
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.quiltmc.qkl.library.text.*
import kotlin.math.floor
import kotlin.math.min

object XpBarRenderer {
    fun generateBar(total: Int, current: Int, length: Int): Text {
        val filledSlices = min(floor((current.toDouble() / total)*length), length.toDouble()).toInt()
        return buildText {
            color(Color.GREEN) {
                literal("[")
                literal(buildString { repeat(filledSlices) { append("|") } })
            }
            literal(buildString { repeat(length-filledSlices) { append("|") } })
            color(Color.GREEN) {
                literal("]")
            }
        }
    }

    fun shouldShowToPlayer(player: ServerPlayerEntity, total: Int, current: Int): Boolean {
        val config = Components.PREFERENCES.get(player)
        return when(config.xpBarShow) {
            XpBarShow.NEVER -> false
            XpBarShow.ALWAYS -> true
            XpBarShow.SMART -> {
                (total.toDouble() / current) % 1 == 0.0
            }
        }
    }

    fun renderForPlayer(player: ServerPlayerEntity, id: Identifier) {
        val components = Components.STATS.get(player)
        val textDisplay = buildText {
            color(Color.GOLD) {
                val name = Components.components[id] ?: return
                translatable(name)
            }
            literal(" ")
            val stat = components.entries[id] ?: return
            styleAndAppend(generateBar(LevelUtils.calculateXpNeededForLevel(stat.level + 1), stat.xp, getXpBarLength(player)).copy())
        }
    }

    fun getXpBarLength(player: ServerPlayerEntity): Int {
        val config = Components.PREFERENCES.get(player)
        return when (config.xpBarLocation) {
            XpBarLocation.TITLE -> 10
            XpBarLocation.SUBTITLE -> 30
            XpBarLocation.HOTBAR -> 20
        }
    }
}