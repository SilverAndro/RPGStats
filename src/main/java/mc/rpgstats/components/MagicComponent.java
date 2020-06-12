package mc.rpgstats.components;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

public class MagicComponent implements IStatComponent {
    private final PlayerEntity player;
    private int xp = 0;
    private int level = 0;

    public MagicComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.level = tag.getInt("level");
        this.xp = tag.getInt("xp");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("xp", this.xp);
        tag.putInt("level", this.level);
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

    @Override
    public int getXP() {
        return this.xp;
    }

    @Override
    public void setXP(int newXP) {
        this.xp = newXP;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(int newLevel) {
        this.level = newLevel;
    }
}
