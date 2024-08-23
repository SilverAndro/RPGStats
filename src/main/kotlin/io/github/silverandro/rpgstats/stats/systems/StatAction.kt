/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.stats.systems

import io.github.silverandro.rpgstats.util.cleanDisplay
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Formatting

interface StatAction {
    fun onLevelUp(
        player: PlayerEntity,
        newLevel: Int,
        hideMessages: Boolean
    )
}

data class StatAttributeAction(
    val stat: RegistryEntry<EntityAttribute>,
    val value: Double,
    val shouldApply: (Int) -> Boolean
) : StatAction {
    override fun onLevelUp(player: PlayerEntity, newLevel: Int, hideMessages: Boolean) {
        if (!shouldApply(newLevel)) return
        if (!hideMessages) {
            player.sendMessage(
                Text.literal("| ").styled { it.withColor(Formatting.GREEN) }
                    .append(Text.literal((if (value > 0) "+" else "-") + value.cleanDisplay + " ").styled { it.withColor(Formatting.YELLOW) })
                    .append(Text.translatable(stat.value().translationKey).styled { it.withColor(Formatting.RESET) }),
                false
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
            Text.literal("| ").styled { it.withColor(Formatting.GREEN) }
                .append(Text.translatable(name).styled { it.withColor(Formatting.YELLOW) })
                .append(Text.literal(" - ").styled { it.withColor(Formatting.RESET) })
                .append(if (descriptionExtra == null) Text.translatable(description) else Text.translatable(description, descriptionExtra)),
            false
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
            Text.literal("| ").styled { it.withColor(Formatting.GREEN) }
                .append(Text.literal((if (fakeValue > 0) "+" else "-") + fakeValue.cleanDisplay + " ").styled { it.withColor(Formatting.YELLOW) })
                .append(Text.translatable(nameTranslationKey).styled { it.withColor(Formatting.RESET) }),
            false
        )
    }
}