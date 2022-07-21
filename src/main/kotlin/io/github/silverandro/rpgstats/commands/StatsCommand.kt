package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.Constants.OPEN_GUI
import io.github.silverandro.rpgstats.LevelUtils
import io.github.silverandro.rpgstats.stats.Components
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.quiltmc.qkl.wrapper.minecraft.brigadier.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.argument.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.required
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.ServerPlayNetworking

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
                literal("for_player"),
                player("targetPlayer")
            ) { _, getTargetPlayer ->
                execute {
                    displayStats(source, getTargetPlayer().value())
                }
            }

            required(literal("gui")) {
                execute {
                    if (ServerPlayNetworking.canSend(source.player, OPEN_GUI)) {
                        ServerPlayNetworking.send(source.player, OPEN_GUI, PacketByteBufs.empty())
                    } else {
                        source.sendError(Text.translatable("rpgstats.error.not_on_client"))
                    }
                }
            }

            required(
                literal("toggleSetting"),
                literal("spamSneak")
            ) { _, _ ->
                execute {
                    val component = Components.PREFERENCES.get(source.player)
                    component.isOptedOutOfButtonSpam = !component.isOptedOutOfButtonSpam
                    source.sendFeedback(
                        Text.translatable(
                            "rpgstats.feedback.toggle_sneak",
                            component.isOptedOutOfButtonSpam
                        ), false
                    )
                }
            }
        }
    }

    private fun displayStats(source: ServerCommandSource, target: ServerPlayerEntity): Int {
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

            Components.components.keys.forEach { identifier ->
                source.sendFeedback(LevelUtils.getFormattedLevelData(identifier, target), false)
            }
        } else {
            source.sendFeedback(Text.translatable("rpgstats.stats_for", target.entityName), false)

            Components.components.keys.forEach { identifier ->
                source.sendFeedback(LevelUtils.getNotFormattedLevelData(identifier, target), false)
            }
        }
        return 1
    }
}