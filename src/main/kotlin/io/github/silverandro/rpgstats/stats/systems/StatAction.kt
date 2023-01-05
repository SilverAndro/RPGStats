/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.stats.systems

import io.github.silverandro.rpgstats.util.cleanDisplay
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.player.PlayerEntity
import org.quiltmc.qkl.library.text.*

interface StatAction {
    fun onLevelUp(
        player: PlayerEntity,
        newLevel: Int,
        hideMessages: Boolean
    )
}

data class StatAttributeAction(
    val stat: EntityAttribute,
    val value: Double,
    val shouldApply: (Int) -> Boolean
) : StatAction {
    override fun onLevelUp(player: PlayerEntity, newLevel: Int, hideMessages: Boolean) {
        if (!shouldApply(newLevel)) return
        if (!hideMessages) {
            player.sendMessage(
                buildText {
                    color(Color.GREEN) { literal("| ") }
                    color(Color.YELLOW) { literal((if (value > 0) "+" else "-") + value.cleanDisplay + " ") }
                    translatable(stat.translationKey)
                }, false
            )
        }
    }
}

data class StatSpecialAction(
    val name: String,
    val description: String,
    val descriptionExtra: Any? = null,
    val shouldApply: (Int) -> Boolean
) : StatAction {
    override fun onLevelUp(player: PlayerEntity, newLevel: Int, hideMessages: Boolean) {
        if (!shouldApply(newLevel) || hideMessages) return
        player.sendMessage(
            buildText {
                color(Color.GREEN) { literal("| ") }
                color(Color.YELLOW) { translatable(name) }
                literal(" - ")
                translatable(description, descriptionExtra ?: arrayOf(0))
            }, false
        )
    }
}

data class StatFakeAttributeAction(
    val nameTranslationKey: String,
    val fakeValue: Double,
    val shouldApply: (Int) -> Boolean
) : StatAction {
    override fun onLevelUp(player: PlayerEntity, newLevel: Int, hideMessages: Boolean) {
        if (!shouldApply(newLevel) || hideMessages) return
        player.sendMessage(
            buildText {
                color(Color.GREEN) { literal("| ") }
                color(Color.YELLOW) { literal((if (fakeValue > 0) "+" else "-") + fakeValue.cleanDisplay + " ") }
                translatable(nameTranslationKey)
            }, false
        )
    }
}