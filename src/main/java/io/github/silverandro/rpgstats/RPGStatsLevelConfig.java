/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats;

import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;

public class RPGStatsLevelConfig extends WrappedConfig {
    public final LevelBuffToggles magic = new LevelBuffToggles();
    public final MeleeBuffToggles melee = new MeleeBuffToggles();
    public final LevelBuffToggles fishing = new LevelBuffToggles();
    public final LevelBuffToggles ranged = new LevelBuffToggles();
    public final DefenseBuffToggles defense = new DefenseBuffToggles();
    public final MiningBuffToggles mining = new MiningBuffToggles();
    public final LevelBuffToggles farming = new LevelBuffToggles();

    public static class LevelBuffToggles implements Config.Section {
        public final boolean enableLv25Buff = true;
        public final boolean enableLv50Buff = true;
    }

    public static class MiningBuffToggles extends LevelBuffToggles {
        @Comment("At what Y level does the lv50 effect trigger?")
        public final int effectLevelTrigger = 20;
    }

    public static class MeleeBuffToggles extends LevelBuffToggles {
        @Comment("How much attack damage is gained per level")
        public final double attackDamagePerLevel = 0.08;
    }

    public static class DefenseBuffToggles extends LevelBuffToggles {
        @Comment("Will only grant HP every X levels")
        public final int everyXLevels = 2;
        @Comment("How much HP to grant on trigger")
        public final int addAmount = 1;
        @Comment("Minimum level before you start getting HP (Exclusive)")
        public final int afterLevel = 10;
    }
}
