package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.quiltmc.qkl.wrapper.minecraft.brigadier.identifier
import org.quiltmc.qkl.wrapper.minecraft.brigadier.players
import org.quiltmc.qkl.wrapper.minecraft.brigadier.register

object CheatCommand {
    enum class OP {
        SET,
        ADD,
        SUB
    }

    fun register(dispatch: CommandDispatcher<ServerCommandSource>) {
        dispatch.register("rpgcheat") {
            requires { it.hasPermissionLevel(2) }
            players("targetPlayers") {
                identifier("skill") {
                    suggests(SkillSuggestionProvider())
                    // TODO("ENUM ARGUMENT TYPE")
                }
            }
        }
    }
}