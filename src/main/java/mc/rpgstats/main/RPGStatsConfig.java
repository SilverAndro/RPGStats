package mc.rpgstats.main;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "rpgstats")
public class RPGStatsConfig implements ConfigData {
    @Comment("Level scaling formula inputs")
    public LevelScaling scaling = new LevelScaling();
    
    @Comment("If players should lose all stats on death")
    public boolean hardcoreMode = false;
    
    @Comment("Toggles for level effects")
    public DefaultLevelToggles toggles = new DefaultLevelToggles();
    
    @Comment("Debug options")
    public Debug debug = new Debug();
    
    public static class LevelScaling {
        @Comment("Default 2.05")
        public double power = 2.05;
        @Comment("Default 0.5")
        public double scale = 0.5;
        @Comment("Default 80")
        public int base = 80;
        @Comment("If the required amount should be the requirements from previous levels combined + new one instead of just solving once")
        public boolean isCumulative = false;
        public int maxLevel = 50;
    }
    
    public static class LevelBuffToggles {
        public boolean enableLv50Buff = true;
        public boolean enableLv25Buff = true;
    }
    
    public static class MiningBuffToggles extends LevelBuffToggles {
        public int effectLevelTrigger = 40;
    }
    
    public static class DefaultLevelToggles {
        public LevelBuffToggles magic = new LevelBuffToggles();
        public LevelBuffToggles melee = new LevelBuffToggles();
        public LevelBuffToggles fishing = new LevelBuffToggles();
        public LevelBuffToggles ranged = new LevelBuffToggles();
        public LevelBuffToggles defense = new LevelBuffToggles();
        public MiningBuffToggles mining = new MiningBuffToggles();
        public LevelBuffToggles farming = new LevelBuffToggles();
    }
    
    public static class Debug {
        public boolean logXpGain = false;
        public boolean logBrokenBlocks = false;
    }
}
