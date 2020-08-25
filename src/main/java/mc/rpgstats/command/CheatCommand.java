package mc.rpgstats.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class CheatCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            // Base
            CommandManager.literal("rpgcheat")
                // Add xp/levels
                .then(
                    CommandManager.literal("add")
                        .then(CommandManager.literal("xp"))
                        .then(CommandManager.literal("levels"))
                )
                // Set xp/levels
                .then(
                    CommandManager.literal("set")
                        .then(CommandManager.literal("xp"))
                        .then(CommandManager.literal("levels"))
                )
        );
    }
    
    private static int executeAdd(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        return 0;
    }
    
    private static int executeSet(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        return 0;
    }
}
