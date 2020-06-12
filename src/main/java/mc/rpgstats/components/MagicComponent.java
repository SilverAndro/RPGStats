package mc.rpgstats.components;

import net.minecraft.nbt.CompoundTag;

public class MagicComponent implements IStatComponent {
    private int value = 0;
    public String name = "magic";

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
        this.value = tag.getInt(this.name);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt(this.name, this.value);
        return tag;
    }
}
