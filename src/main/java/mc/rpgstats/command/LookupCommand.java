package mc.rpgstats.command;

import com.mojang.brigadier.CommandDispatcher;
import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.literal;

public class LookupCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("rpglookup")
                        .then(CommandManager.argument("targetPlayer", player())
                                .then(literal("level").then(CommandManager.argument("skillId", identifier())
                                        .suggests(new SkillSuggestionProvider())
                                        .executes(context -> doLookup(getPlayer(context, "targetPlayer"), "level", getIdentifier(context, "skillId")))))
                                .then(literal("xp").then(CommandManager.argument("skillId", identifier())
                                        .suggests(new SkillSuggestionProvider())
                                        .executes(context -> doLookup(getPlayer(context, "targetPlayer"), "xp", getIdentifier(context, "skillId")))))
                                .then(literal("total_xp").then(CommandManager.argument("skillId", identifier())
                                        .suggests(new SkillSuggestionProvider())
                                        .executes(context -> doLookup(getPlayer(context, "targetPlayer"), "total_xp", getIdentifier(context, "skillId")))))
                                .then(literal("highest_level")
                                        .executes(context -> doLookup(getPlayer(context, "targetPlayer"), "highest_level", null)))
                                .then(literal("lowest_level")
                                        .executes(context -> doLookup(getPlayer(context, "targetPlayer"), "lowest_level", null)))
                                .then(literal("highest_total_xp")
                                        .executes(context -> doLookup(getPlayer(context, "targetPlayer"), "highest_total_xp", null)))
                                .then(literal("lowest_total_xp")
                                        .executes(context -> doLookup(getPlayer(context, "targetPlayer"), "lowest_total_xp", null)))
        ));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static int doLookup(ServerPlayerEntity player, String type, Identifier skillId) {
        return switch (type) {
            case "level" -> RPGStats.getComponentLevel(skillId, player);
            case "xp" -> RPGStats.getComponentXP(skillId, player);
            case "total_xp" -> RPGStats.getTotalXp(skillId, player);
            case "highest_level" -> RPGStats.getHighestLevel(player);
            case "lowest_level" -> RPGStats.getStatLevelsForPlayer(player).stream().max(Integer::compareTo).get();
            case "highest_total_xp" -> CustomComponents.components.keySet().stream().map(identifier -> RPGStats.getTotalXp(identifier, player)).max(Integer::compareTo).get();
            case "lowest_total_xp" -> CustomComponents.components.keySet().stream().map(identifier -> RPGStats.getTotalXp(identifier, player)).min(Integer::compareTo).get();
            default -> -1;
        };
    }
}
