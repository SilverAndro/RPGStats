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
        val attrInst = player.getAttributeInstance(stat)
        if (attrInst != null) {
            attrInst.baseValue += value
        }
        if (!hideMessages) {
            player.sendMessage(
                Text.literal(
                    (if (value > 0) "+" else "-") + value.cleanDisplay
                ).formatted(Formatting.GREEN)
                    .append(Text.translatable(stat.translationKey)),
                false
            )
        }
    }
}

data class StatSpecialAction(
    val name: String,
    val description: String,
    val shouldApply: (Int) -> Boolean
) : StatAction {
    override fun onLevelUp(player: PlayerEntity, newLevel: Int, hideMessages: Boolean) {
        if (!shouldApply(newLevel) || hideMessages) return
        player.sendMessage(
            Text.literal(name).formatted(Formatting.GREEN)
                .append(
                    Text.of(description)
                ), false
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
            Text.literal(
                (if (fakeValue > 0) "+" else "-") + fakeValue.cleanDisplay
            ).formatted(Formatting.GREEN)
                .append(Text.translatable(nameTranslationKey)),
            false
        )
    }
}