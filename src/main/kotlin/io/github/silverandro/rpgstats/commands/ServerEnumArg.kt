package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

inline fun <reified E : Enum<E>> ArgumentBuilder<ServerCommandSource, *>.thenEnum(cascade: ArgumentBuilder<ServerCommandSource, *>.(E)->Unit) {
    enumValues<E>().forEach {
        val literal = CommandManager.literal(it.name)
        cascade(literal, it)
        then(literal)
    }
}