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

    private fun modifyXpAndLevels(
        players: Collection<ServerPlayerEntity>,
        skillId: Identifier,
        operation: Operation,
        type: Type,
        amount: Int
    ) {
        if (operation == Operation.SET) {
            if (type == Type.LEVELS) {
                players.forEach {
                    LevelUtils.setComponentLevel(skillId, it, amount)
                }
            }
            if (type == Type.XP) {
                players.forEach {
                    LevelUtils.setComponentXP(skillId, it, amount)
                    // TODO: Replace with a proper method for checking if someone should level up, this is kind of a hack
                    LevelUtils.addXpAndLevelUp(skillId, it, 0)
                }
            }
        }
        if (operation == Operation.ADD) {
            if (type == Type.LEVELS) {
                players.forEach {
                    LevelUtils.levelUp(skillId, it, amount)
                }
            }
            if (type == Type.XP) {
                players.forEach {
                    LevelUtils.addXpAndLevelUp(skillId, it, amount)
                }
            }
        }
        if (operation == Operation.SUBTRACT) {
            if (type == Type.LEVELS) {
                TODO("Add removing levels, preserve XP ratio?")
            }
            if (type == Type.XP) {
                players.forEach {
                    LevelUtils.removeXp(skillId, it, amount)
                }
            }
        }
    }
}