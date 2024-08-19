/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.display

import io.github.silverandro.rpgstats.util.filterInPlace
import kotlinx.coroutines.*
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

object LevelUpDisplays {
    private val displayScope = CoroutineScope(Dispatchers.Default)
    val activeDisplays = mutableMapOf<UUID, Job>()

    init {
        displayScope.launch {
            while (isActive) {
                delay(20.milliseconds)
                activeDisplays.filterInPlace { _, job -> !job.isActive }
            }
        }
    }

    fun fancyLevelUpDisplay(player: ServerPlayerEntity) {
        activeDisplays[player.uuid] = displayScope.launch {
            var currentOffset = 0.0f
            var currentDistance = 1.3
            var count = 0
            while (isActive && count < 50) {
                val points = generatePointsAroundCircle(player.pos.add(0.0, count.toDouble() / 35, 0.0), 8, currentDistance, currentOffset)
                val connection = player.networkHandler
                for (point in points) {
                    connection.sendPacket(
                        ParticleS2CPacket(
                            ParticleTypes.FIREWORK,
                            true,
                            point.x,
                            point.y,
                            point.z,
                            0.0f,
                            0.0f,
                            0.0f,
                            0.0f,
                            1
                        )
                    )
                }
                count++
                currentOffset += 0.1f
                currentDistance -= 0.015
                delay(15.milliseconds)
            }
        }
    }

    fun standardLevelUpDisplay(player: ServerPlayerEntity) {
        val points = generatePointsAroundCircle(player.pos.add(0.0, 0.5, 0.0), 16, 1.1)
        val connection = player.networkHandler
        for (point in points) {
            connection.sendPacket(
                ParticleS2CPacket(
                    ParticleTypes.HAPPY_VILLAGER,
                    true,
                    point.x,
                    point.y,
                    point.z,
                    0.0f,
                    0.0f,
                    0.0f,
                    0.1f,
                    1
                )
            )
        }
    }

    private fun generatePointsAroundCircle(origin: Vec3d, pointCount: Int, distance: Double, angleOffset: Float = 0.0f): List<Vec3d> {
        val step: Float = ((Math.PI * 2) / pointCount).toFloat()
        var current = angleOffset
        val stop = Math.PI * 2 + angleOffset
        return buildList {
            while (current < stop) {
                add(Vec3d(origin.x + (sin(current) * distance), origin.y, origin.z + (cos(current) * distance)))
                current += step
            }
        }
    }
}