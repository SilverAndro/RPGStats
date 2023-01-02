package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.LevelUtils
import io.github.silverandro.rpgstats.stats.Components
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.quiltmc.qkl.library.brigadier.CommandResult
import org.quiltmc.qkl.library.brigadier.argument.enum
import org.quiltmc.qkl.library.brigadier.argument.identifier
import org.quiltmc.qkl.library.brigadier.argument.player
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.executeWithResult
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.required

@Command
object LookupCommand {
    enum class LookupType {
        LEVEL,
        XP,
        TOTAL_XP,
    }

    enum class LookupTypeNoSkill {
        HIGHEST_LEVEL,
        LOWEST_LEVEL,
        HIGHEST_TOTAL_XP,
        LOWEST_TOTAL_XP
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("rpglookup") {
            requires { it.hasPermissionLevel(2) }
            required(player("targetPlayer")) {targetPlayer ->
                required(enum("skillLookup", LookupType::class), identifier("skillId")) {lookupType, skillId ->
                    suggests(SkillSuggestionProvider())
                    executeWithResult { CommandResult.success(preformLookup(targetPlayer().value(), lookupType().value(), skillId().value())) }
                }
                required(enum("generalLookup", LookupTypeNoSkill::class)) {lookupType ->
                    executeWithResult { CommandResult.success(preformLookup(targetPlayer().value(), lookupType().value())) }
                }
            }
        }
    }

    private fun preformLookup(player: ServerPlayerEntity, lookup: LookupTypeNoSkill): Int {
        return when(lookup) {
            LookupTypeNoSkill.HIGHEST_LEVEL -> LevelUtils.getHighestLevel(player)
            LookupTypeNoSkill.LOWEST_LEVEL -> LevelUtils.getLowestLevel(player)
            LookupTypeNoSkill.HIGHEST_TOTAL_XP -> Components.components.keys.maxOf { LevelUtils.getCumulativeXp(it, player) }
            LookupTypeNoSkill.LOWEST_TOTAL_XP -> Components.components.keys.minOf { LevelUtils.getCumulativeXp(it, player) }
        }
    }

    private fun preformLookup(player: ServerPlayerEntity, lookup: LookupType, skillId: Identifier): Int {
        return when(lookup) {
            LookupType.LEVEL -> LevelUtils.getComponentLevel(skillId, player)
            LookupType.XP -> LevelUtils.getComponentXP(skillId, player)
            LookupType.TOTAL_XP -> LevelUtils.getCumulativeXp(skillId, player)
        }
    }
}