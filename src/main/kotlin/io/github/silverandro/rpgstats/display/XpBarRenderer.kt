/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.display

import io.github.silverandro.rpgstats.LevelUtils
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
import kotlin.math.round
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object XpBarRenderer {
    private val activeBarsScope = CoroutineScope(Dispatchers.Default)
    val activeBars = mutableMapOf<UUID, Job>()

    private val smartIndices = DoubleArray(30) { it/30.0 }.filter { it.isFinite() }.map { round(it * 1000) / 1000 }.toSet()

    init {
        activeBarsScope.launch {
            while (isActive) {
                delay(20.milliseconds)
                activeBars.filterInPlace { _, job -> !job.isActive }
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

    fun shouldShowToPlayer(player: ServerPlayerEntity, total: Int, current: Int, previous: Int): Boolean {
        val config = Components.PREFERENCES.get(player)
        return when(config.xpBarShow) {
            XpBarShow.NEVER -> false
            XpBarShow.ALWAYS -> true
            XpBarShow.SMART -> {
                val amounts = smartIndices.map { round(total * it).toInt() }
                val previousIndex = amounts.indexOfFirst { it > previous } - 1
                val currentIndex = amounts.indexOfFirst { it > current } - 1
                currentIndex != previousIndex
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