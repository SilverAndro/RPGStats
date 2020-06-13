package mc.rpgstats.components;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class RangedComponent implements IStatComponent {
    private final PlayerEntity player;
    private int xp = 0;
    private int level = 0;

    public RangedComponent(PlayerEntity player) {
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
        return RPGStats.RANGED_COMPONENT;
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

    @Override
    public String getName() {
        return "ranged";
    }

    @Override
    public void onLevelUp() {
        player.sendMessage(new LiteralText("§a+1§r Bow accuracy"), false);
    }
}
