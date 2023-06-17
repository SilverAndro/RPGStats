/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.silverandro.rpgstats.stats.Components
import io.github.silverandro.rpgstats.stats.internal.XpBarLocation
import io.github.silverandro.rpgstats.stats.internal.XpBarShow
import io.github.silverandro.rpgstats.util.supplier
import mc.rpgstats.hooky_gen.api.Command
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.quiltmc.qkl.library.brigadier.argument.boolean
import org.quiltmc.qkl.library.brigadier.argument.enum
import org.quiltmc.qkl.library.brigadier.argument.literal
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.required
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.literal
import org.quiltmc.qkl.library.text.translatable

@Command
object PreferencesCommand {
    fun register(dispatch: CommandDispatcher<ServerCommandSource>) {
        dispatch.register("rpgconfig") {
            required(literal("disable_spam"), boolean("disable_button_spam")) { _, boolean ->
                execute {
                    val component = Components.PREFERENCES.get(source.playerOrThrow)
                    component.isOptedOutOfButtonSpam = boolean().value()
                    source.sendFeedback(
                        Text.translatable(
                            "rpgstats.feedback.toggle_sneak",
                            component.isOptedOutOfButtonSpam
                        ).supplier(), false
                    )
                }
            }

            required(literal("xp_bar")) {
                required(literal("location"), enum("location_value", XpBarLocation::class)) { _, enum ->
                    execute {
                        val component = Components.PREFERENCES.get(source.playerOrThrow)
                        component.xpBarLocation = enum().value()
                        source.sendFeedback(
                            Text.translatable(
                                "rpgstats.feedback.xp_bar_location",
                                component.xpBarLocation.name
                            ).supplier(), false
                        )
                    }
                }
                required(literal("show"), enum("show_value", XpBarShow::class)) { _, enum ->
                    execute {
                        val component = Components.PREFERENCES.get(source.player)
                        component.xpBarShow = enum().value()
                        source.sendFeedback(
                            Text.translatable(
                                "rpgstats.feedback.xp_bar_show",
                                component.xpBarShow.name
                            ).supplier(), false
                        )
                    }
                }
            }

            execute {
                val component = Components.PREFERENCES.get(source.player)
                val feedback = buildText {
                    translatable("rpgstats.feedback.toggle_sneak", component.isOptedOutOfButtonSpam)
                    literal("\n")
                    translatable("rpgstats.feedback.xp_bar_location", component.xpBarLocation.name)
                    literal("\n")
                    translatable("rpgstats.feedback.xp_bar_show", component.xpBarShow.name)
                }
                source.sendFeedback(feedback.supplier(), false)
            }
        }
    }
}