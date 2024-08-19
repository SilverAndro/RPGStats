/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.LevelUtils
import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.util.supplier
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

@Command
object StatsCommand {
    fun register(dispatch: CommandDispatcher<ServerCommandSource>) {
        dispatch.register(literal("rpgstats")
            .then(argument("targetPlayer", EntityArgumentType.player())
                .executes {
                    return@executes displayStats(
                        it.source,
                        EntityArgumentType.getPlayer(it, "targetPlayer")
                    )
                })
            .executes {
                return@executes displayStats(
                    it.source,
                    it.source.playerOrThrow
                )
            })
    }

    private fun displayStats(source: ServerCommandSource, target: ServerPlayerEntity): Int {
        val statsToShow = Components.components.filter { it.value.shouldShowToUser || source.hasPermissionLevel(2) }
        if (source.entity != null) {
            source.sendFeedback(
                Text.literal("RPGStats > ").styled { it.withFormatting(Formatting.GREEN) }
                    .append(Text.translatable("rpgstats.stats_for", target.gameProfile.name)).supplier(),
                false
            )

           statsToShow.forEach { (identifier, entry) ->
                source.sendFeedback(LevelUtils.getLevelDisplay(identifier, target, !entry.shouldShowToUser).supplier(), false)
            }
        } else {
            source.sendFeedback(Text.translatable("rpgstats.stats_for", target.gameProfile.name).supplier(), false)

            statsToShow.forEach { (identifier, entry) ->
                source.sendFeedback(LevelUtils.getLevelDisplay(identifier, target, !entry.shouldShowToUser).supplier(), false)
            }
        }

        // Return the amount of stats shown
        return statsToShow.size
    }
}