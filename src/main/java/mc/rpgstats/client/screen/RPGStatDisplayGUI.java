package mc.rpgstats.client.screen;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.RPGStatsClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class RPGStatDisplayGUI extends LightweightGuiDescription {
    ArrayList<Identifier> data = new ArrayList<>();
    
    public RPGStatDisplayGUI() {
        super();
        data.addAll(RPGStatsClient.currentStats.keySet());
        
        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(240, 180);
        
        WLabel guiTitle = new WLabel(Text.of("  RPGStats"));
        root.add(guiTitle, 5, 1);
    
        BiConsumer<Identifier, StatEntry> configurator = (Identifier identifier, StatEntry entry) -> {
            int level = RPGStatsClient.currentStats.get(identifier).getLeft();
            int xp = RPGStatsClient.currentStats.get(identifier).getRight();
            
            String name = RPGStatsClient.nameMap.get(identifier);
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            entry.name.setText(
                    Text.literal(name)
                            .formatted(Formatting.DARK_AQUA).formatted(Formatting.BOLD)
            );
            entry.level.setText(Text.literal("Level: ").formatted(Formatting.DARK_GREEN).append(String.valueOf(level)));
            entry.xp.setText(Text.literal("XP: ").formatted(Formatting.DARK_GREEN).append(xp + "/" + RPGStats.calculateXpNeededToReachLevel(level + 1)));
        };
    
        WListPanel<Identifier, StatEntry> list = new WListPanel<>(data, StatEntry::new, configurator);
        list.setListItemHeight(18);
        root.add(list, 1, 2, 11, 7);
        
        root.validate(this);
    }
    
    public static class StatEntry extends WPlainPanel {
        WLabel name;
        WLabel level;
        WLabel xp;
        
        public StatEntry() {
            name = new WLabel(Text.literal("Foo"));
            this.add(name, 0, 0, 5*18, 18);
            level = new WLabel(Text.literal("0"));
            this.add(level, 60, 0, 6*18, 18);
            xp = new WLabel(Text.literal("0/0"));
            this.add(xp, 120, 0, 6*18, 18);
            
            this.setSize(7*18, 2*18);
        }
    }
}
