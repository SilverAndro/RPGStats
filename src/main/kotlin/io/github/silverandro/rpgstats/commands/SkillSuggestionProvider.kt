package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.silverandro.rpgstats.stats.Components
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

class SkillSuggestionProvider : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for (skillID in Components.components.keys) {
            builder.suggest(skillID.toString())
        }
        return builder.buildFuture()
    }
}