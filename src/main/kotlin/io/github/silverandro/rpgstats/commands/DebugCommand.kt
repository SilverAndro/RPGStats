package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.datafixers.util.Either
import io.github.silverandro.rpgstats.datadrive.xp.XpData
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.server.command.ServerCommandSource
import org.quiltmc.qkl.library.brigadier.argument.blockPos
import org.quiltmc.qkl.library.brigadier.argument.entity
import org.quiltmc.qkl.library.brigadier.argument.literal
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.required
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.literal
import kotlin.jvm.optionals.getOrNull

@Command
@OptIn(ExperimentalStdlibApi::class)
object DebugCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("rpgdebug") {
            requires { it.hasPermissionLevel(2) }
            required(literal("rea")) {
                required(literal("block"), blockPos("blockPos")) { _, blockPos ->
                    execute {
                        val entry = XpData.BLOCK_XP[source.world.getBlockState(blockPos().value()).block].getOrNull()
                        displayRea(source, entry)
                    }
                }
                required(literal("entity"), entity("entitySelected")) { _, entitySelected ->
                    execute {
                        val entry = XpData.ENTITY_XP_OVERRIDE[entitySelected().value().type].getOrNull()
                        displayRea(source, entry)
                    }
                }
            }
        }
    }

    private fun displayRea(source: ServerCommandSource, entry:  Either<XpData.XpEntry, MutableList<XpData.XpEntry>>?) {
        if (entry != null) {
            entry.ifLeft {
                source.sendFeedback(buildText {
                    literal(it.id.toString() + "\n")
                    literal(" - Amount: ${it.amount}\n")
                    literal(" - Chance: ${it.chance}")
                }, false)
            }.ifRight {
                source.sendFeedback(buildText {
                    it.forEach {
                        literal(it.id.toString() + "\n")
                        literal(" - Amount: ${it.amount}\n")
                        literal(" - Chance: ${it.chance}")
                    }
                }, false)
            }
        } else {
            source.sendFeedback(buildText { color(Color.RED) { literal("No REA registered!") } }, false)
        }
    }
}