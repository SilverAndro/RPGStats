package io.github.silverandro.rpgstats;

import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;

public class RPGStatsDamageBlacklist extends WrappedConfig {
    @Comment("If damage from this source should be allowed to grant XP")
    public final boolean inFire = true;
    public final boolean lightning = true;
    public final boolean onFire = true;
    public final boolean lava = true;
    public final boolean hotFloor = true;
    public final boolean inWall = true;
    public final boolean cramming = true;
    public final boolean drown = true;
    public final boolean starve = true;
    public final boolean cactus = true;
    public final boolean fall = true;
    public final boolean flyIntoWall = true;
    public final boolean outOfWorld = true;
    public final boolean magic = true;
    public final boolean generic = true;
    public final boolean wither = true;
    public final boolean anvil = true;
    public final boolean fallingBlock = true;
    public final boolean dryOut = true;
    public final boolean berryBush = true;
    public final boolean freeze = true;
    public final boolean stalactite = true;
    public final boolean fallingStalactite = true;
}
