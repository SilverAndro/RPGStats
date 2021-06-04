package mc.rpgstats.component;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class StatsEntry {
    public final Identifier id;
    public int level;
    public int xp;
    
    public StatsEntry(Identifier identifier, int level, int xp) {
        this.id = identifier;
        this.level = level;
        this.xp = xp;
    }
    
    public void toCompound(NbtCompound compoundTag) {
        NbtCompound tag = new NbtCompound();
        tag.putInt("level", level);
        tag.putInt("xp", xp);
        compoundTag.put(id.toString(), tag);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatsEntry entry = (StatsEntry)o;
        return level == entry.level && xp == entry.xp && id.equals(entry.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, level, xp);
    }
}
