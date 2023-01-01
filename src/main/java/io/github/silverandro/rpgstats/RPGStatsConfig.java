package io.github.silverandro.rpgstats;

import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.FloatRange;
import org.quiltmc.config.api.annotations.IntegerRange;

public class RPGStatsConfig extends WrappedConfig {
    @Comment("Level scaling formula inputs")
    public final LevelScaling scaling = new LevelScaling();

    @Comment("If players should lose all stats on death")
    public final boolean hardcoreMode = false;

    @Comment("Settings for the anticheat implementation")
    public final AntiCheat antiCheat = new AntiCheat();

    @Comment("Debug logging config")
    public final Debug debug = new Debug();

    public static class AntiCheat implements Config.Section {
        @Comment("Prevent duplicate XP from breaking blocks in the same location")
        public final boolean blockBreakPos = true;
        @Comment("How many ticks before you gain XP from breaking a block from a location again")
        @IntegerRange(min = -1, max = Integer.MAX_VALUE)
        public final int blockBreakDelay = 5000;
    }

    public static class LevelScaling implements Config.Section {
        @FloatRange(min = 0.0001, max = Double.MAX_VALUE)
        public final double power = 2.07;
        @FloatRange(min = 0.0001, max = Double.MAX_VALUE)
        public final double scale = 0.52;
        @IntegerRange(min = 1, max = Integer.MAX_VALUE)
        public final int base = 80;
        @Comment("If the required amount should be the requirements from previous levels combined + new one instead of just solving once")
        public final boolean isCumulative = false;
        @Comment("The maximum level allowed")
        @IntegerRange(min = 0, max = Integer.MAX_VALUE)
        public final int maxLevel = 50;
    }

    public static class Debug implements Config.Section {
        public final boolean logXpGain = false;
        public final boolean logBrokenBlocks = false;
        public final boolean logRawOps = false;
        public final boolean logRawWrite = false;
        public final boolean logAntiCheatPrevention = false;
    }
}
