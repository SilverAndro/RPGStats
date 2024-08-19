/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.serialization.Codec
import io.github.silverandro.rpgstats.LevelUtils
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.EnumArgumentType
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import kotlin.math.floor

@Command
object CheatCommand {
    enum class Operation : StringIdentifiable {
        SET,
        ADD,
        SUBTRACT;

        override fun asString() = this.name

        companion object {
            val CODEC: Codec<Operation> = StringIdentifiable.createCodec { entries.toTypedArray() }
        }
    }

    enum class Type : StringIdentifiable {
        XP,
        LEVELS;

        override fun asString() = this.name

        companion object {
            val CODEC: Codec<Type> = StringIdentifiable.createCodec { entries.toTypedArray() }
        }
    }

    private val operationArg = object : EnumArgumentType<Operation>(Operation.CODEC, { Operation.entries.toTypedArray() }) {}
    private val typeArg = object : EnumArgumentType<Type>(Type.CODEC, { Type.entries.toTypedArray() }) {}

    fun register(dispatch: CommandDispatcher<ServerCommandSource>) {
        dispatch.register(literal("rpgcheat")
            .requires {it.hasPermissionLevel(2)}
            .then(argument("targetPlayers", EntityArgumentType.players())
                .then(argument("skill", IdentifierArgumentType.identifier())
                    .suggests(SkillSuggestionProvider())
                    .then(argument("operation", operationArg)
                        .then(argument("type", typeArg)
                            .then(argument("amount", IntegerArgumentType.integer(0))
                                .executes {
                                    return@executes modifyXpAndLevels(
                                        EntityArgumentType.getPlayers(it, "targetPlayers"),
                                        IdentifierArgumentType.getIdentifier(it, "skill"),
                                        it.getArgument("operation", Operation::class.java),
                                        it.getArgument("type", Type::class.java),
                                        IntegerArgumentType.getInteger(it, "amount")
                                    )
                                }))))))
    }

    private fun modifyXpAndLevels(
        players: Collection<ServerPlayerEntity>,
        skillId: Identifier,
        operation: Operation,
        type: Type,
        amount: Int
    ): Int {
        if (operation == Operation.SET) {
            if (type == Type.LEVELS) {
                players.forEach {
                    LevelUtils.setComponentLevel(skillId, it, amount)
                }
            }
            if (type == Type.XP) {
                players.forEach {
                    LevelUtils.setComponentXP(skillId, it, amount)
                    LevelUtils.addXpAndLevelUp(skillId, it, 0)
                }
            }
            return amount
        }
        if (operation == Operation.ADD) {
            var total = 0
            if (type == Type.LEVELS) {
                players.forEach {
                    val original = LevelUtils.getComponentLevel(skillId, it)
                    LevelUtils.levelUp(skillId, it, amount)
                    total += LevelUtils.getComponentLevel(skillId, it) - original
                }
            }
            if (type == Type.XP) {
                players.forEach {
                    val original = LevelUtils.getComponentXP(skillId, it)
                    LevelUtils.addXpAndLevelUp(skillId, it, amount)
                    total += LevelUtils.getComponentXP(skillId, it) - original
                }
            }
            return total
        }
        if (operation == Operation.SUBTRACT) {
            var total = 0
            if (type == Type.LEVELS) {
                players.forEach {
                    val ratio = LevelUtils.getComponentXP(skillId, it).toDouble() /
                                LevelUtils.calculateXpNeededForLevel(
                                    LevelUtils.getComponentLevel(skillId, it) + 1
                                ).toDouble()
                    val originalLevel = LevelUtils.getComponentLevel(skillId, it)
                    LevelUtils.setComponentLevel(skillId, it, originalLevel - amount)
                    LevelUtils.setComponentXP(skillId, it,
                        floor(LevelUtils.calculateXpNeededForLevel(
                            LevelUtils.getComponentLevel(skillId, it)) * ratio
                        ).toInt()
                    )
                    total += originalLevel - LevelUtils.getComponentLevel(skillId, it)
                }
            }
            if (type == Type.XP) {
                // TODO: Proper return count
                players.forEach {
                    LevelUtils.removeXp(skillId, it, amount)
                    total += LevelUtils.getComponentXP(skillId, it)
                }
            }
            return total
        }
        return 0
    }
}