package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.LevelUtils
import io.github.silverandro.rpgstats.stats.Components
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.quiltmc.qkl.library.brigadier.argument.player
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required

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
                execute {
                    displayStats(source, player().value())
                }
            }
        }
    }

    private fun displayStats(source: ServerCommandSource, target: ServerPlayerEntity): Int {
        val statsToShow = Components.components.filter { it.value.shouldShowToUser || source.hasPermissionLevel(2) }
        if (source.entity != null) {
            source.sendFeedback(
                Text.literal("RPGStats > ")
                    .formatted(Formatting.GREEN)
                    .append(
                        Text.translatable("rpgstats.stats_for", target.entityName)
                            .formatted(Formatting.WHITE)
                    ),
                false
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