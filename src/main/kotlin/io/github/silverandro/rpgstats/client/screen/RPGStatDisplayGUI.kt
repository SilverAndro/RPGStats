package io.github.silverandro.rpgstats.client.screen

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WListPanel
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.silverandro.rpgstats.LevelUtils.calculateXpNeededToReachLevel
import io.github.silverandro.rpgstats.client.RPGStatsClient.currentStats
import io.github.silverandro.rpgstats.client.RPGStatsClient.nameMap
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*
import java.util.function.BiConsumer

class RPGStatDisplayGUI : LightweightGuiDescription() {
    var data = mutableListOf<Identifier>()

    init {
        data.addAll(currentStats.keys)
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(240, 180)
        val guiTitle = WLabel(Text.of("  RPGStats"))
        root.add(guiTitle, 5, 1)
        val configurator = BiConsumer { identifier: Identifier?, entry: StatEntry ->
            val level = currentStats[identifier]!!
                .left
            val xp = currentStats[identifier]!!.right
            var name = nameMap[identifier]
            name = name!!.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1)
            entry.name.text = Text.literal(name)
                .formatted(Formatting.DARK_AQUA).formatted(Formatting.BOLD)
            entry.level.text = Text.literal("Level: ").formatted(Formatting.DARK_GREEN).append(level.toString())
            entry.xp.text = Text.literal("XP: ").formatted(Formatting.DARK_GREEN)
                .append(xp.toString() + "/" + calculateXpNeededToReachLevel(level + 1))
        }
        val list = WListPanel(data as List<Identifier>, { StatEntry() }, configurator)
        list.setListItemHeight(18)
        root.add(list, 1, 2, 11, 7)
        root.validate(this)
    }

    class StatEntry : WPlainPanel() {
        var name: WLabel
        var level: WLabel
        var xp: WLabel

        init {
            name = WLabel(Text.literal("Foo"))
            this.add(name, 0, 0, 5 * 18, 18)
            level = WLabel(Text.literal("0"))
            this.add(level, 60, 0, 6 * 18, 18)
            xp = WLabel(Text.literal("0/0"))
            this.add(xp, 120, 0, 6 * 18, 18)
            setSize(7 * 18, 2 * 18)
        }
    }
}