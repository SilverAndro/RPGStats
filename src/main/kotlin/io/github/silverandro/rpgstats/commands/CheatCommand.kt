package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.LevelUtils
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.quiltmc.qkl.wrapper.minecraft.brigadier.argument.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.execute
import org.quiltmc.qkl.wrapper.minecraft.brigadier.register
import org.quiltmc.qkl.wrapper.minecraft.brigadier.required
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.required

object CheatCommand {
    enum class Operation {
        SET,
        ADD,
        SUBTRACT
    }

    enum class Type {
        XP,
        LEVELS
    }

    fun register(dispatch: CommandDispatcher<ServerCommandSource>) {
        dispatch.register("rpgcheat") {
            requires { it.hasPermissionLevel(2) }
            required(players("targetPlayers")) { getTargetPlayers ->
                required(identifier("skill")) { getSkillIdentifier ->
                    suggests(SkillSuggestionProvider())
                    required(
                        enum("operation", Operation::class),
                        enum("type", Type::class),
                        integer("amount", min = 0)
                    ) { getOperation, getType, getAmount ->
                        execute {
                            modifyXpAndLevels(
                                getTargetPlayers().required(),
                                getSkillIdentifier().value(),
                                getOperation().value(),
                                getType().value(),
                                getAmount().value()
                            )
                        }
                    }
                }
            }
        }
    }

    fun modifyXpAndLevels(
        players: Collection<ServerPlayerEntity>,
        skillId: Identifier,
        operation: Operation,
        type: Type,
        amount: Int
    ) {
        if (operation == Operation.ADD) {
            if (type == Type.LEVELS) {
                repeat(amount) {
                    TODO()
                }
            }
        }
    }
}