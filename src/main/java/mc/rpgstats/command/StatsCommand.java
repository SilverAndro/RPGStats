package mc.rpgstats.command;

import com.mojang.brigadier.CommandDispatcher;
import mc.rpgstats.component.internal.PlayerPreferencesComponent;
import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class StatsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("rpgstats")
                .executes(
                    (commandContext) -> execute(
                        commandContext.getSource(), (ServerPlayerEntity)commandContext.getSource().getEntity()
                    )
                ).then(
                CommandManager.literal("GUI").executes(
                    (commandContext) -> {
                        ServerCommandSource source = commandContext.getSource();
                        ServerPlayerEntity player = source.getPlayer();
                        if (ServerPlayNetworking.canSend(player, RPGStats.OPEN_GUI)) {
                            ServerPlayNetworking.send(player, RPGStats.OPEN_GUI, PacketByteBufs.empty());
                            return 1;
                        } else {
                            player.sendMessage(new LiteralText("You don't have RPGStats installed on your client, cant open GUI").formatted(Formatting.RED), false);
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
                            PlayerPreferencesComponent component = CustomComponents.PREFERENCES.get(context.getSource().getPlayer());
                            component.isOptedOutOfButtonSpam = !component.isOptedOutOfButtonSpam;
                            context.getSource().sendFeedback(new LiteralText("Hold sneak instead of spam: " + component.isOptedOutOfButtonSpam), false);
                            return 1;
                        }
                    )
                )
            )
        );
    }
    
    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        if (source.getEntity() instanceof ServerPlayerEntity && target != null) {
            ServerPlayerEntity spe = (ServerPlayerEntity)source.getEntity();
            
            spe.sendMessage(new LiteralText("§aRPGStats >§r Stats for " + target.getEntityName()), false);
            
            CustomComponents.customComponents.keySet().forEach(identifier -> {
                spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(identifier, target)), false);
            });
        } else if (target != null) {
            if (source.getEntity() == null) {
                source.sendFeedback(new LiteralText("Stats for " + target.getEntityName()), false);
    
                CustomComponents.customComponents.keySet().forEach(identifier -> {
                    source.sendFeedback(new LiteralText(RPGStats.getNotFormattedLevelData(identifier, target)), false);
                });
            } else {
                ServerPlayerEntity spe = (ServerPlayerEntity)source.getEntity();
                ServerPlayerEntity targeted = (ServerPlayerEntity)source.getEntity();
                
                spe.sendMessage(new LiteralText("§aRPGStats >§r Stats for " + targeted.getEntityName()), false);
    
                CustomComponents.customComponents.keySet().forEach(identifier -> {
                    spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(identifier, targeted)), false);
                });
            }
        } else {
            source.sendError(new LiteralText("A player must be passed when execute from the console"));
        }
        return 1;
    }
}
