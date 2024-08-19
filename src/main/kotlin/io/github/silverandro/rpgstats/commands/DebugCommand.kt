/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.datadrive.xp.XpData
import io.github.silverandro.rpgstats.util.supplier
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

@Command
@OptIn(ExperimentalStdlibApi::class)
object DebugCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal("rpgdebug").requires {it.hasPermissionLevel(2)}
            .then(literal("rea")
                .then(literal("block")
                    .then(argument("blockPos", BlockPosArgumentType.blockPos())
                        .executes {
                            val entry = XpData.BLOCK_XP[it.source.world.getBlockState(BlockPosArgumentType.getBlockPos(it, "blockPos")).block]
                            displayRea(it.source, entry)
                            return@executes 0
                        }))
                .then(literal("entity")
                    .then(argument("entitySelected", EntityArgumentType.entity())
                        .executes {
                            val entry = XpData.ENTITY_XP_OVERRIDE[EntityArgumentType.getEntity(it, "entitySelected").type]
                            displayRea(it.source, entry)
                            return@executes 0
                        }))))
    }

    private fun displayRea(source: ServerCommandSource, entry: List<XpData.XpEntry>?) {
        if (entry != null) {
            var output = Text.empty()
            entry.forEachIndexed { index, it ->
                output = output.append(Text.of(it.id.toString() + "\n")).append(" - Amount: ${it.amount}\n").append(" - Chance: ${it.chance}")
                if (index != entry.lastIndex) {
                    output = output.append("\n")
                }
            }

            source.sendFeedback(output.supplier(), false)
        } else {
            source.sendError(Text.of("No REA registered!"))
        }
    }
}