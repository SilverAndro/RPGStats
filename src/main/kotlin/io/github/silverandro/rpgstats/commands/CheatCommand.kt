package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.quiltmc.qkl.wrapper.minecraft.brigadier.argument.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.execute
import org.quiltmc.qkl.wrapper.minecraft.brigadier.register
import org.quiltmc.qkl.wrapper.minecraft.brigadier.required

object CheatCommand {
    enum class OP {
        SET,
        ADD,
        SUB
    }

    fun register(dispatch: CommandDispatcher<ServerCommandSource>) {
        dispatch.register("rpgcheat") {
            requires { it.hasPermissionLevel(2) }
            required(players("targetPlayers")) { getTargetPlayers ->
                required(identifier("skill")) { getSkillIdentifier ->
                    suggests(SkillSuggestionProvider())
                    required(enum("operation", OP::class)) { getOperation ->
                        execute {
                            val targets = getTargetPlayers().required()
                            val identifier = getSkillIdentifier().value()
                            val operation = getOperation().value()
                        }
                    }
                }
            }
        }
    }
}