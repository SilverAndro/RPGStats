package mc.rpgstats.main;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "rpgstats")
public class RPGStatsConfig implements ConfigData {
    LevelScaling scaling = new LevelScaling();
    
    @Comment("If players should lose all stats on death")
    public boolean hardcoreMode = false;
    
    public static class LevelScaling {
        @Comment("Default 2.05")
        double power = 2.05;
        @Comment("Default 0.5")
        double scale = 0.5;
        @Comment("Default 80")
        int base = 80;
    }
}
