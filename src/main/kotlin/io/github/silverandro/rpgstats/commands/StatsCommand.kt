/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.LevelUtils
import io.github.silverandro.rpgstats.stats.Components
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.quiltmc.qkl.library.brigadier.CommandResult
import org.quiltmc.qkl.library.brigadier.argument.player
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.executeWithResult
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.text.*

@Command
object StatsCommand {
    fun register(dispatch: CommandDispatcher<ServerCommandSource>) {
        dispatch.register("rpgstats") {
            executes {
                displayStats(
                    it.source,
                    it.source.player
                )
            }
            required(
                player("targetPlayer")
            ) { player ->
                executeWithResult {
                    CommandResult.success(displayStats(source, player().value()))
                }
            }
        }
    }

    private fun displayStats(source: ServerCommandSource, target: ServerPlayerEntity): Int {
        val statsToShow = Components.components.filter { it.value.shouldShowToUser || source.hasPermissionLevel(2) }
        if (source.entity != null) {
            source.sendFeedback(
                buildText {
                    color(Color.GREEN) {
                        literal("RPGStats > ")
                    }
                    translatable("rpgstats.stats_for", target.entityName)
                }, false
            )

           statsToShow.forEach { (identifier, entry) ->
                source.sendFeedback(LevelUtils.getLevelDisplay(identifier, target, !entry.shouldShowToUser), false)
            }
        } else {
            source.sendFeedback(Text.translatable("rpgstats.stats_for", target.entityName), false)

            statsToShow.forEach { (identifier, entry) ->
                source.sendFeedback(LevelUtils.getLevelDisplay(identifier, target, !entry.shouldShowToUser), false)
            }
        }

        // Return the amount of stats shown
        return statsToShow.size
    }
}