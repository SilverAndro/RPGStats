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

    @Comment("Config for how HP scaling works with defense")
    public DefenseHPConfig defenseHP = new DefenseHPConfig();

    public MeleeAttackConfig melee = new MeleeAttackConfig();

    @Comment("If these damage types should grant defense XP when blocked")
    public DamageSourceBlacklist damageBlacklist = new DamageSourceBlacklist();


    @Comment("Options for attempting to prevent cheating")
    public AntiCheat antiCheat = new AntiCheat();

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

    public static class AntiCheat {
        @Comment("Prevent duplicate XP from breaking blocks in the same location")
        public boolean blockBreakPos = true;
        @Comment("How many ticks before you gain XP from breaking a block from a location again")
        public int blockBreakDelay = 5000;
    }

    public static class LevelBuffToggles {
        public boolean enableLv50Buff = true;
        public boolean enableLv25Buff = true;
    }

    public static class DamageSourceBlacklist {
        public boolean inFire = true;
        public boolean lightning = true;
        public boolean onFire = true;
        public boolean lava = true;
        public boolean hotFloor = true;
        public boolean inWall = true;
        public boolean cramming = true;
        public boolean drown = true;
        public boolean starve = true;
        public boolean cactus = true;
        public boolean fall = true;
        public boolean flyIntoWall = true;
        public boolean outOfWorld = true;
        public boolean magic = true;
        public boolean generic = true;
        public boolean wither = true;
        public boolean anvil = true;
        public boolean fallingBlock = true;
        public boolean dryOut = true;
        public boolean berryBush = true;
        public boolean freeze = true;
        public boolean stalactite = true;
        public boolean fallingStalactite = true;
    }

    public static class DefenseHPConfig {
        @Comment("Will only grant HP every X levels")
        public int everyXLevels = 2;
        @Comment("How much HP to grant on trigger")
        public int addAmount = 1;
        @Comment("Minimum level before you start getting HP (Exclusive)")
        public int afterLevel = 10;
    }

    public static class MeleeAttackConfig {
        public double attackDamagePerLevel = 0.08;
    }

    public static class MiningBuffToggles extends LevelBuffToggles {
        @Comment("At what Y level does the lv50 effect trigger?")
        public int effectLevelTrigger = 20;
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
        public boolean logRawOps = false;
        public boolean logRawWrite = false;

        public boolean logAntiCheatPrevention = false;
    }
}
