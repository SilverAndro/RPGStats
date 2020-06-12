package mc.rpgstats.components;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

public class MagicComponent implements IStatComponent {
    private final PlayerEntity player;
    private int value = 0;

    public MagicComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public void setValue(int newValue) {
        this.value = newValue;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.value = tag.getInt("value");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("value", this.value);
        return tag;
    }

    @Override
    public Entity getEntity() {
        return player;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return RPGStats.MAGIC_COMPONENT;
    }
}
