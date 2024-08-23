/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.stats.internal.XpBarLocation
import io.github.silverandro.rpgstats.stats.internal.XpBarShow
import io.github.silverandro.rpgstats.util.supplier
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

@Command
object PreferencesCommand {
    private val xpLocationArgs = literal("location").apply {
        thenEnum<XpBarLocation> { location ->
            executes {
                val component = Components.PREFERENCES.get(it.source.playerOrThrow)
                component.xpBarLocation = location
                it.source.sendFeedback(
                    Text.translatable(
                        "rpgstats.feedback.xp_bar_location",
                        component.xpBarLocation.name
                    ).supplier(), false
                )
                return@executes 0
            }
        }
    }

    private val xpShowArgs = literal("show").apply {
        thenEnum<XpBarShow> { show ->
            executes {
                val component = Components.PREFERENCES.get(it.source.playerOrThrow)
                component.xpBarShow = show
                it.source.sendFeedback(
                    Text.translatable(
                        "rpgstats.feedback.xp_bar_show",
                        component.xpBarShow.name
                    ).supplier(), false
                )
                return@executes 0
            }
        }
    }

    fun register(dispatch: CommandDispatcher<ServerCommandSource>) {
        dispatch.register(literal("rpgconfig").then(
            literal("disable_spam").then(
                argument("disable_button_spam", BoolArgumentType.bool()).executes {
                    val component = Components.PREFERENCES.get(it.source.playerOrThrow)
                    component.isOptedOutOfButtonSpam = BoolArgumentType.getBool(it, "disable_button_spam")
                    it.source.sendFeedback(
                        Text.translatable(
                            "rpgstats.feedback.toggle_sneak",
                            component.isOptedOutOfButtonSpam
                        ).supplier(), false
                    )

                    return@executes 0
                })).then(
                    literal("xp_bar")
                        .then(xpLocationArgs)
                        .then(xpShowArgs)
                ).executes {
                    val component = Components.PREFERENCES.get(it.source.playerOrThrow)
                    val feedback = Text.translatable("rpgstats.feedback.toggle_sneak", component.isOptedOutOfButtonSpam)
                        .append("\n")
                        .append(Text.translatable("rpgstats.feedback.xp_bar_location", component.xpBarLocation.name))
                        .append("\n")
                        .append(Text.translatable("rpgstats.feedback.xp_bar_show", component.xpBarShow.name))
                    it.source.sendFeedback(feedback.supplier(), false)
                    return@executes 0
        })
    }
}