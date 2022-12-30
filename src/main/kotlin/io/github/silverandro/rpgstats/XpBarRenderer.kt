package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.stats.internal.XpBarLocation
import io.github.silverandro.rpgstats.stats.internal.XpBarShow
import kotlinx.coroutines.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.quiltmc.qkl.library.text.*
import kotlin.math.floor
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

object XpBarRenderer {
    val activeBarsScope = CoroutineScope(Dispatchers.Default)
    val activeBars = mutableMapOf<ServerPlayerEntity, Job>()

    init {
        activeBarsScope.launch {
            while (isActive) {
                delay(0.1.seconds)
                val toRemove = mutableListOf<ServerPlayerEntity>()
                activeBars.forEach { player, job -> if (job.isActive.not()) toRemove.add(player) }
                toRemove.forEach { activeBars.remove(it) }
            }
        }
    }

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
        val config = Components.PREFERENCES.get(player)
        val textDisplay = buildText {
            color(Color.GOLD) {
                val name = Components.components[id] ?: return
                translatable(name)
            }
            literal(" ")
            val stat = components.entries[id] ?: return
            styleAndAppend(generateBar(LevelUtils.calculateXpNeededForLevel(stat.level + 1), stat.xp, getXpBarLength(player)).copy())
        }

        val job = activeBarsScope.launch {
            repeat(8) {
                when (config.xpBarLocation) {
                    XpBarLocation.TITLE -> TODO()
                    XpBarLocation.SUBTITLE -> TODO()
                    XpBarLocation.HOTBAR -> player.sendMessage(textDisplay, true)
                }
                delay(0.5.seconds)
            }
        }
        activeBars[player]?.cancel(CancellationException("Superseded by new xp bar"))
        activeBars[player] = job
    }

    fun getXpBarLength(player: ServerPlayerEntity): Int {
        val config = Components.PREFERENCES.get(player)
        return when (config.xpBarLocation) {
            XpBarLocation.TITLE -> 20
            XpBarLocation.SUBTITLE -> 30
            XpBarLocation.HOTBAR -> 40
        }
    }
}