package io.github.silverandro.rpgstats.stats.systems

import io.github.silverandro.rpgstats.util.cleanDisplay
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.player.PlayerEntity
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
    val stat: EntityAttribute,
    val value: Double,
    val shouldApply: (Int) -> Boolean
) : StatAction {
    override fun onLevelUp(player: PlayerEntity, newLevel: Int, hideMessages: Boolean) {
        if (!shouldApply(newLevel)) return
        if (!hideMessages) {
            player.sendMessage(
                Text.literal("| ").formatted(Formatting.GREEN)
                    .append(Text.literal((if (value > 0) "+" else "-") + value.cleanDisplay + " ").formatted(Formatting.YELLOW))
                    .append(Text.translatable(stat.translationKey).formatted(Formatting.WHITE)),
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
            Text.literal("| ").formatted(Formatting.GREEN)
                .append(Text.translatable(name).formatted(Formatting.YELLOW))
                .append(Text.literal(" - ").formatted(Formatting.WHITE))
                .append(Text.translatable(description, descriptionExtra ?: arrayOf(0)).formatted(Formatting.WHITE)),
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
            Text.literal("| ").formatted(Formatting.GREEN)
                .append(Text.literal((if (fakeValue > 0) "+" else "-") + fakeValue.cleanDisplay + " ").formatted(Formatting.YELLOW))
                .append(Text.translatable(nameTranslationKey).formatted(Formatting.WHITE)),
            false
        )
    }
}