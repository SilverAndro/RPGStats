package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.Constants.OPEN_GUI
import io.github.silverandro.rpgstats.LevelUtils
import io.github.silverandro.rpgstats.stats.Components
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.quiltmc.qkl.wrapper.minecraft.brigadier.literal
import org.quiltmc.qkl.wrapper.minecraft.brigadier.player
import org.quiltmc.qkl.wrapper.minecraft.brigadier.register
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
            literal("for_player") {
                player("targetPlayer") {
                    executes {
                        displayStats(it.source, EntityArgumentType.getPlayer(it, "targetPlayer"))
                    }
                }
            }
            literal("gui") {
                executes {
                    if (ServerPlayNetworking.canSend(it.source.player, OPEN_GUI)) {
                        ServerPlayNetworking.send(it.source.player, OPEN_GUI, PacketByteBufs.empty())
                        1
                    } else {
                        it.source.sendError(Text.translatable("rpgstats.error.not_on_client"))
                        0
                    }
                }
            }
            literal("toggleSetting") {
                literal("spamSneak") {
                    executes {
                        val component = Components.PREFERENCES.get(it.source.player)
                        component.isOptedOutOfButtonSpam = !component.isOptedOutOfButtonSpam
                        it.source.sendFeedback(
                            Text.translatable(
                                "rpgstats.feedback.toggle_sneak",
                                component.isOptedOutOfButtonSpam
                            ), false
                        )
                        1
                    }
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