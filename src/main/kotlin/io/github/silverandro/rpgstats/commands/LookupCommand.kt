/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.serialization.Codec
import io.github.silverandro.rpgstats.LevelUtils
import io.github.silverandro.rpgstats.stats.Components
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable

@Command
object LookupCommand {
    enum class LookupType : StringIdentifiable {
        LEVEL,
        XP,
        TOTAL_XP;

        override fun asString() = this.name

        companion object {
            val CODEC: Codec<LookupType> = StringIdentifiable.createCodec { entries.toTypedArray() }
        }
    }

    enum class LookupTypeNoSkill : StringIdentifiable {
        HIGHEST_LEVEL,
        LOWEST_LEVEL,
        HIGHEST_TOTAL_XP,
        LOWEST_TOTAL_XP;

        override fun asString() = this.name

        companion object {
            val CODEC: Codec<LookupTypeNoSkill> = StringIdentifiable.createCodec { entries.toTypedArray() }
        }
    }

    private val args = argument("targetPlayer", EntityArgumentType.player()).apply {
        thenEnum<LookupType> {
            then(argument("skillId", IdentifierArgumentType.identifier())
                .suggests(SkillSuggestionProvider())
                .executes {
                    val player = EntityArgumentType.getPlayer(it, "targetPlayer")
                    val lookup = it.getArgument("skillLookup", LookupType::class.java)
                    val skillId = IdentifierArgumentType.getIdentifier(it, "skillId")
                    return@executes preformLookup(player, lookup, skillId)
                }
            )
        }
        thenEnum<LookupTypeNoSkill> {
            executes {
                val player = EntityArgumentType.getPlayer(it, "targetPlayer")
                val lookup = it.getArgument("generalLookup", LookupTypeNoSkill::class.java)
                return@executes preformLookup(player, lookup)
            }
        }
    }


    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal("rpglookup")
            .requires { it.hasPermissionLevel(2) }
            .then(args)
        )
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