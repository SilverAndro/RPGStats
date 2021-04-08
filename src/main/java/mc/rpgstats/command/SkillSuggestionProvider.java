package mc.rpgstats.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class SkillSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        for (Identifier skillID : CustomComponents.oldComponentIdToComponentIndexMap.keySet()) {
            builder.suggest('"' + skillID.toString() + '"');
        }
        return builder.buildFuture();
    }
}
