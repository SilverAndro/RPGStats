package mc.rpgstats.client.screen;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import mc.rpgstats.component.IStatComponent;
import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.RPGStatsClient;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class RPGStatDisplayGUI extends LightweightGuiDescription {
    ArrayList<IStatComponent> data = new ArrayList<>();
    
    public RPGStatDisplayGUI() {
        data.addAll(RPGStatsClient.currentStats.keySet());
        
        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(240, 180);
        
        WLabel guiTitle = new WLabel("  RPGStats");
        root.add(guiTitle, 5, 0);
    
        BiConsumer<IStatComponent, StatEntry> configurator = (IStatComponent component, StatEntry entry) -> {
            int level = RPGStatsClient.currentStats.get(component).getLeft();
            int xp = RPGStatsClient.currentStats.get(component).getRight();
            
            entry.name.setText(new LiteralText(component.getCapName()).formatted(Formatting.DARK_AQUA).formatted(Formatting.BOLD));
            entry.level.setText(new LiteralText("Level: ").formatted(Formatting.DARK_GREEN).append(String.valueOf(level)));
            entry.xp.setText(new LiteralText("XP: ").formatted(Formatting.DARK_GREEN).append(xp + "/" + RPGStats.calculateXpNeededToReachLevel(level + 1)));
        };
    
        WListPanel<IStatComponent, StatEntry> list = new WListPanel<>(data, StatEntry::new, configurator);
        list.setListItemHeight(18);
        root.add(list, 1, 1, 11, 8);
        
        root.validate(this);
    }
    
    public static class StatEntry extends WPlainPanel {
        WLabel name;
        WLabel level;
        WLabel xp;
        
        public StatEntry() {
            name = new WLabel("Foo");
            this.add(name, 0, 18, 5*18, 18);
            level = new WLabel("0");
            this.add(level, 60, 18, 6*18, 18);
            xp = new WLabel("0/0");
            this.add(xp, 120, 18, 6*18, 18);
            
            this.setSize(7*18, 2*18);
        }
    }
}