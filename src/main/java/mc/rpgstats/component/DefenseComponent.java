package mc.rpgstats.component;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;

import java.util.Objects;

public class DefenseComponent implements IStatComponent {
    private final PlayerEntity player;
    private int xp = 0;
    private int level = 0;
    
    public DefenseComponent(PlayerEntity player) {
        this.player = player;
    }
    
    @Override
    public void readFromNbt(CompoundTag tag) {
        this.level = tag.getInt("level");
        this.xp = tag.getInt("xp");
    }
    
    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putInt("xp", this.xp);
        tag.putInt("level", this.level);
    }
    
    @Override
    public Entity getEntity() {
        return player;
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
        return "defense";
    }
    
    @Override
    public String getCapName() {
        return "Defense";
    }
    
    @Override
    public void onLevelUp(boolean beQuiet) {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) + 0.01);
        if (!beQuiet)
            player.sendMessage(new LiteralText("§a+0.01§r Knockback resistance"), false);
        if (getLevel() % 2 == 0 && getLevel() > 10) {
            Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) + 1);
            if (!beQuiet)
                player.sendMessage(new LiteralText("§a+1§r Health"), false);
        }
        
        if (!beQuiet) {
            if (level == 25) {
                player.sendMessage(new LiteralText("§aNimble§r - 5% chance to avoid damage"), false);
            } else if (level == 50) {
                player.sendMessage(new LiteralText("§aNimble II§r - 10% chance to avoid damage"), false);
            }
        }
    }
}
