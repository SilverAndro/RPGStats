package mc.rpgstats.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Objects;

public class StatsComponent implements Component {
    private PlayerEntity playerEntity;
    
    public HashMap<Identifier, StatsEntry> entries = new HashMap<>();
    
    public StatsComponent(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }
    
    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        entries.clear();
        compoundTag.getKeys().forEach(s -> {
            Identifier identifier = Identifier.tryParse(s);
            if (identifier != null) {
                CompoundTag data = compoundTag.getCompound(identifier.toString());
                assert data != null;
                entries.put(identifier, new StatsEntry(identifier, data.getInt("level"), data.getInt("xp")));
            } else {
                System.err.println("Failed to parse stat identifier: " + s);
            }
        });
    }
    
    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        for (StatsEntry entry : entries.values()) {
            entry.toCompound(compoundTag);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatsComponent that = (StatsComponent)o;
        return playerEntity.equals(that.playerEntity) && entries.equals(that.entries);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(playerEntity, entries);
    }
    
    public StatsEntry getOrCreateID(Identifier id) {
        return entries.computeIfAbsent(id, identifier -> new StatsEntry(id, 0, 0));
    }
}
