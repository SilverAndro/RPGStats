package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

inline fun <reified E : Enum<E>> ArgumentBuilder<ServerCommandSource, *>.thenEnum(cascade: ArgumentBuilder<ServerCommandSource, *>.()->Unit) {
    enumValues<E>().forEach {
        @Suppress("UNCHECKED_CAST")
        cascade(then(CommandManager.literal(it.name) as ArgumentBuilder<ServerCommandSource, *>) as ArgumentBuilder<ServerCommandSource, *>)
    }
}