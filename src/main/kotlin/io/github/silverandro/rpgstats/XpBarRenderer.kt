package io.github.silverandro.rpgstats

import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.stats.internal.XpBarLocation
import io.github.silverandro.rpgstats.stats.internal.XpBarShow
import io.github.silverandro.rpgstats.util.filterInPlace
import kotlinx.coroutines.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.quiltmc.qkl.library.text.*
import java.util.*
import kotlin.math.floor
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object XpBarRenderer {
    val activeBarsScope = CoroutineScope(Dispatchers.Default)
    val activeBars = mutableMapOf<UUID, Job>()

    init {
        activeBarsScope.launch {
            while (isActive) {
                delay(50.milliseconds)
                activeBars.filterInPlace { uuid, job -> !job.isActive }
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
                // TODO Improve this
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
                translatable(name.translationKey)
            }
            literal(" ")
            val stat = components.entries[id] ?: return
            styleAndAppend(generateBar(LevelUtils.calculateXpNeededForLevel(stat.level + 1), stat.xp, getXpBarLength(player)).copy())
        }

        val job = activeBarsScope.launch {
            when (config.xpBarLocation) {
                XpBarLocation.CHAT -> player.sendMessage(textDisplay, false)
                XpBarLocation.HOTBAR -> {
                    repeat(20) {
                        if (player.isDisconnected) cancel(CancellationException("Player disconnected"))
                        player.sendMessage(textDisplay, true)
                        delay(0.2.seconds)
                    }
                }
            }
        }
        activeBars[player.uuid]?.cancel(CancellationException("Superseded by new xp bar"))
        activeBars[player.uuid] = job
    }

    fun getXpBarLength(player: ServerPlayerEntity): Int {
        val config = Components.PREFERENCES.get(player)
        return when (config.xpBarLocation) {
            XpBarLocation.CHAT -> 50
            XpBarLocation.HOTBAR -> 40
        }
    }
}