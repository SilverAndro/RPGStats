package mc.rpgstats.component;

import io.github.silverandro.rpgstats.Constants;
import mc.rpgstats.main.RPGStats;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class StatsEntry {
    public final Identifier id;
    private int level;
    private int xp;
    
    public StatsEntry(Identifier identifier, int level, int xp) {
        this.id = identifier;
        this.setLevel(level);
        this.setXp(xp);
    }
    
    public void toCompound(NbtCompound compoundTag) {
        NbtCompound tag = new NbtCompound();
        tag.putInt("level", getLevel());
        tag.putInt("xp", getXp());
        compoundTag.put(id.toString(), tag);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatsEntry entry = (StatsEntry)o;
        return getLevel() == entry.getLevel() && getXp() == entry.getXp() && id.equals(entry.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, getLevel(), getXp());
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        if (RPGStats.getConfig().debug.logRawWrite) {
            Constants.INSTANCE.getDebugLogger().info("Im "+id.toString()+" and my level is now " + level);
        }
        this.level = level;
    }
    
    public int getXp() {
        return xp;
    }
    
    public void setXp(int xp) {
        if (RPGStats.getConfig().debug.logRawWrite) {
            Constants.INSTANCE.getDebugLogger().info("Im "+id.toString()+" and my xp is now " + xp);
        }
        this.xp = xp;
    }
}