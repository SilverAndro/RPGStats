package mc.rpgstats.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import mc.rpgstats.event.LevelUpCallback;
import mc.rpgstats.main.RPGStats;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Collection;

import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class CheatCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            // Base
            CommandManager.literal("rpgcheat")
                // OP only
                .requires((serverCommandSource) -> serverCommandSource.hasPermissionLevel(2))
                .then(
                    // Target player
                    CommandManager.argument("targets", EntityArgumentType.players())
                        .then(
                            // Select skill
                            CommandManager.argument("skill", string())
                                .suggests(new SkillSuggestionProvider())
                                // Add xp/levels
                                .then(
                                    CommandManager.literal("add")
                                        .then(
                                            CommandManager.literal("xp")
                                                .then(
                                                    CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(
                                                            (commandContext) -> executeAdd(
                                                                commandContext.getSource(),
                                                                new Identifier(StringArgumentType.getString(commandContext, "skill")),
                                                                EntityArgumentType.getPlayers(commandContext, "targets"),
                                                                CommandType.XP,
                                                                IntegerArgumentType.getInteger(commandContext, "amount")
                                                            )
                                                        )
                                                )
                                        )
                                        .then(
                                            CommandManager.literal("levels")
                                                .then(
                                                    CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(
                                                            (commandContext) -> executeAdd(
                                                                commandContext.getSource(),
                                                                new Identifier(StringArgumentType.getString(commandContext, "skill")),
                                                                EntityArgumentType.getPlayers(commandContext, "targets"),
                                                                CommandType.LEVELS,
                                                                IntegerArgumentType.getInteger(commandContext, "amount")
                                                            )
                                                        )
                                                )
                                        )
                                )
                                // Set xp/levels
                                .then(
                                    CommandManager.literal("set")
                                        // Set XP
                                        .then(
                                            CommandManager.literal("xp")
                                                .then(
                                                    CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(
                                                            (commandContext) -> executeSet(
                                                                commandContext.getSource(),
                                                                new Identifier(StringArgumentType.getString(commandContext, "skill")),
                                                                EntityArgumentType.getPlayers(commandContext, "targets"),
                                                                CommandType.XP,
                                                                IntegerArgumentType.getInteger(commandContext, "amount")
                                                            )
                                                        )
                                                )
                                        )
                                        // Set Levels
                                        .then(
                                            CommandManager.literal("levels")
                                                .then(
                                                    CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(
                                                            (commandContext) -> executeSet(
                                                                commandContext.getSource(),
                                                                new Identifier(StringArgumentType.getString(commandContext, "skill")),
                                                                EntityArgumentType.getPlayers(commandContext, "targets"),
                                                                CommandType.LEVELS,
                                                                IntegerArgumentType.getInteger(commandContext, "amount")
                                                            )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
    
    private static int executeAdd(ServerCommandSource source, Identifier id, Collection<ServerPlayerEntity> targets, CommandType type, int amount) {
        for (ServerPlayerEntity target : targets) {
            if (type == CommandType.XP) {
                RPGStats.addXpAndLevelUp(id, target, amount);
            }
            if (type == CommandType.LEVELS) {
                RPGStats.setComponentLevel(id, target, RPGStats.getComponentXP(id, target) + amount);
            }
        }
        source.sendFeedback(new LiteralText(amount + " XP added to stat " + id + " for " + targets.size() + " targets."), true);
        return 1;
    }
    
    private static int executeSet(ServerCommandSource source, Identifier id, Collection<ServerPlayerEntity> targets, CommandType type, int amount) {
        for (ServerPlayerEntity target : targets) {
            if (type == CommandType.XP) {
                RPGStats.setComponentXP(id, target, amount);
            }
            if (type == CommandType.LEVELS) {
                RPGStats.setComponentLevel(id, target, amount);
                LevelUpCallback.EVENT.invoker().onLevelUp(target, id, amount, true);
            }
        }
        source.sendFeedback(new LiteralText("XP set for stat " + id + " to " + amount + " for " + targets.size() + " targets."), true);
        return 1;
    }
    
    enum CommandType {
        LEVELS,
        XP
    }
}
