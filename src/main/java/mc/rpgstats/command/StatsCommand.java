package mc.rpgstats.command;

import com.mojang.brigadier.CommandDispatcher;
import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.StatComponents;
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
                        if (ServerPlayNetworking.canSend(player, RPGStats.OPEN_GUI) && false) {
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
            )
        );
    }
    
    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        if (source.getEntity() instanceof ServerPlayerEntity && target != null) {
            ServerPlayerEntity spe = (ServerPlayerEntity)source.getEntity();
            
            spe.sendMessage(new LiteralText("§aRPGStats >§r Stats for " + target.getEntityName()), false);
            
            spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.MELEE_COMPONENT, target)), false);
            spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.RANGED_COMPONENT, target)), false);
            spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.DEFENSE_COMPONENT, target)), false);
            spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.MAGIC_COMPONENT, target)), false);
            spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.FARMING_COMPONENT, target)), false);
            spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.MINING_COMPONENT, target)), false);
            spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.FISHING_COMPONENT, target)), false);
        } else if (target != null) {
            if (source.getEntity() == null) {
                source.sendFeedback(new LiteralText("Stats for " + target.getEntityName()), false);
                
                source.sendFeedback(new LiteralText(RPGStats.getNotFormattedLevelData(StatComponents.MELEE_COMPONENT, target)), false);
                source.sendFeedback(new LiteralText(RPGStats.getNotFormattedLevelData(StatComponents.RANGED_COMPONENT, target)), false);
                source.sendFeedback(new LiteralText(RPGStats.getNotFormattedLevelData(StatComponents.DEFENSE_COMPONENT, target)), false);
                source.sendFeedback(new LiteralText(RPGStats.getNotFormattedLevelData(StatComponents.MAGIC_COMPONENT, target)), false);
                source.sendFeedback(new LiteralText(RPGStats.getNotFormattedLevelData(StatComponents.FARMING_COMPONENT, target)), false);
                source.sendFeedback(new LiteralText(RPGStats.getNotFormattedLevelData(StatComponents.MINING_COMPONENT, target)), false);
                source.sendFeedback(new LiteralText(RPGStats.getNotFormattedLevelData(StatComponents.FISHING_COMPONENT, target)), false);
            } else {
                ServerPlayerEntity spe = (ServerPlayerEntity)source.getEntity();
                ServerPlayerEntity targeted = (ServerPlayerEntity)source.getEntity();
                
                spe.sendMessage(new LiteralText("§aRPGStats >§r Stats for " + targeted.getEntityName()), false);
                
                spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.MELEE_COMPONENT, targeted)), false);
                spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.RANGED_COMPONENT, targeted)), false);
                spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.DEFENSE_COMPONENT, targeted)), false);
                spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.MAGIC_COMPONENT, targeted)), false);
                spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.FARMING_COMPONENT, targeted)), false);
                spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.MINING_COMPONENT, targeted)), false);
                spe.sendMessage(new LiteralText(RPGStats.getFormattedLevelData(StatComponents.FISHING_COMPONENT, targeted)), false);
            }
        } else {
            source.sendError(new LiteralText("A player must be passed when execute from the console"));
        }
        return 1;
    }
}