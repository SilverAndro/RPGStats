package mc.rpgstats.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.util.Identifier;

import java.util.List;
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
    
    public void toCompound(CompoundTag compoundTag) {
        List<Integer> array = new java.util.ArrayList<>(level);
        array.add(xp);
        
        IntArrayTag data = new IntArrayTag(array);
        
        compoundTag.put(id.toString(), data);
        
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
