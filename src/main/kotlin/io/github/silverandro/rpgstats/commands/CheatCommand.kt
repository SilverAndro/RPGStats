package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.LevelUtils
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.quiltmc.qkl.library.brigadier.CommandResult
import org.quiltmc.qkl.library.brigadier.argument.*
import org.quiltmc.qkl.library.brigadier.executeWithResult
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.required
import kotlin.math.floor

@Command
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
                        executeWithResult {
                            val count = modifyXpAndLevels(
                                getTargetPlayers().required(),
                                getSkillIdentifier().value(),
                                getOperation().value(),
                                getType().value(),
                                getAmount().value()
                            )
                            CommandResult.success(count)
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
                    // TODO: Replace with a proper method for checking if someone should level up, this is kind of a hack
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