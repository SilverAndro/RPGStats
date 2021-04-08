package mc.rpgstats.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class StatsComponent implements Component {
    private PlayerEntity playerEntity;
    
    public ArrayList<StatsEntry> entries = new ArrayList<>();
    
    public StatsComponent(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }
    
    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        entries.clear();
        compoundTag.getKeys().forEach(s -> {
            Identifier identifier = Identifier.tryParse(s);
            if (identifier != null) {
                int[] tagEntry = compoundTag.getIntArray(s);
                entries.add(new StatsEntry(Identifier.tryParse(s), tagEntry[0], tagEntry[1]));
            } else {
                System.err.println("Failed to parse stat identifier: " + s);
            }
        });
    }
    
    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        for (StatsEntry entry : entries) {
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
    
    public StatsEntry getOrSetFromID(Identifier id) {
        Optional<StatsEntry> possible = entries.stream().filter(statsEntry -> statsEntry.id == id).findFirst();
        if (possible.isPresent()) {
            return possible.get();
        } else {
            StatsEntry entry = new StatsEntry(id, 0, 0);
            entries.add(entry);
            return entry;
        }
    }
}
