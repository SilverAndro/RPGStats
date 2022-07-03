package mc.rpgstats.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.silverandro.rpgstats.Constants;
import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class StatsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("rpgstats")
                        .executes(
                                (commandContext) -> execute(
                                        commandContext.getSource(), (ServerPlayerEntity) commandContext.getSource().getEntity()
                                )
                        ).then(
                                CommandManager.literal("GUI").executes(
                                        (commandContext) -> {
                                            ServerCommandSource source = commandContext.getSource();
                                            ServerPlayerEntity player = source.getPlayer();
                                            if (ServerPlayNetworking.canSend(player, Constants.INSTANCE.getOPEN_GUI())) {
                                                ServerPlayNetworking.send(player, Constants.INSTANCE.getOPEN_GUI(), PacketByteBufs.empty());
                                                return 1;
                                            } else {
                                                source.sendError(Text.translatable("rpgstats.error.not_on_client"));
                                                return 0;
                                            }
                                        }
                                )
                        ).then(CommandManager.literal("for")
                                .then(
                                        CommandManager.argument("targets", EntityArgumentType.player())
                                                .executes(
                                                        (commandContext) -> execute(
                                                                commandContext.getSource(), EntityArgumentType.getPlayer(commandContext, "targets")
                                                        )
                                                )
                                )
                        ).then(CommandManager.literal("toggleSetting")
                                .then(CommandManager.literal("spamSneak")
                                        .executes(
                                                context -> {
                                                    PlayerPreferencesComponent component = Components.PREFERENCES.get(context.getSource().getPlayer());
                                                    component.isOptedOutOfButtonSpam = !component.isOptedOutOfButtonSpam;
                                                    context.getSource().sendFeedback(Text.translatable("rpgstats.feedback.toggle_sneak", component.isOptedOutOfButtonSpam), false);
                                                    return 1;
                                                }
                                        )
                                )
                        )
        );
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        if (source.getEntity() instanceof ServerPlayerEntity spe && target != null) {

            spe.sendMessage(Text.translatable("RPGStats > ")
                    .formatted(Formatting.GREEN)
                    .append(Text.translatable("rpgstats.stats_for", target.getEntityName()).formatted(Formatting.WHITE)), false);

            Components.components.keySet().forEach(identifier ->
                    spe.sendMessage(LevelUtils.INSTANCE.getFormattedLevelData(identifier, target), false)
            );
        } else if (target != null) {
            if (source.getEntity() == null) {
                source.sendFeedback(Text.translatable("rpgstats.stats_for", target.getEntityName()), false);

                Components.components.keySet().forEach(identifier ->
                        source.sendFeedback(LevelUtils.INSTANCE.getNotFormattedLevelData(identifier, target), false)
                );
            } else {
                ServerPlayerEntity spe = (ServerPlayerEntity) source.getEntity();
                ServerPlayerEntity targeted = (ServerPlayerEntity) source.getEntity();

                spe.sendMessage(Text.literal("RPGStats > ")
                        .formatted(Formatting.GREEN)
                        .append(Text.translatable("rpgstats.stats_for", target.getEntityName()).formatted(Formatting.WHITE)), false);

                Components.components.keySet().forEach(identifier ->
                        spe.sendMessage(LevelUtils.INSTANCE.getFormattedLevelData(identifier, targeted), false)
                );
            }
        } else {
            source.sendError(Text.translatable("rpgstats.error.console_player_required"));
        }
        return 1;
    }
}