package mc.rpgstats.main;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "rpgstats")
public class RPGStatsConfig implements ConfigData {
    @Comment("Level scaling formula inputs")
    public LevelScaling scaling = new LevelScaling();
    
    @Comment("If players should lose all stats on death")
    public boolean hardcoreMode = false;
    
    @Comment("Toggles for level effects")
    public DefaultLevelToggles toggles = new DefaultLevelToggles();
    
    public static class LevelScaling {
        @Comment("Default 2.05")
        public double power = 2.05;
        @Comment("Default 0.5")
        public double scale = 0.5;
        @Comment("Default 80")
        public int base = 80;
    }
    
    public static class LevelBuffToggles {
        public boolean enableLv50Buff = true;
        public boolean enableLv25Buff = true;
    }
    
    public static class DefaultLevelToggles {
        public LevelBuffToggles magic = new LevelBuffToggles();
        public LevelBuffToggles melee = new LevelBuffToggles();
        public LevelBuffToggles fishing = new LevelBuffToggles();
        public LevelBuffToggles ranged = new LevelBuffToggles();
        public LevelBuffToggles defense = new LevelBuffToggles();
        public LevelBuffToggles mining = new LevelBuffToggles();
        public LevelBuffToggles farming = new LevelBuffToggles();
    }
}
