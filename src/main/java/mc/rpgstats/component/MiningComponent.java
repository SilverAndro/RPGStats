package mc.rpgstats.component;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;

import java.util.Objects;

public class MiningComponent implements IStatComponent {
    private final PlayerEntity player;
    private int xp = 0;
    private int level = 0;
    
    public MiningComponent(PlayerEntity player) {
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
        return "mining";
    }
    
    @Override
    public String getCapName() {
        return "Mining";
    }
    
    @Override
    public void onLevelUp(boolean beQuiet) {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_LUCK)).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_LUCK) + 0.05);
        
        if (!beQuiet) {
            player.sendMessage(new LiteralText("§a+0.05§r Luck"), false);
            
            if (level == 25) {
                player.sendMessage(new LiteralText("§aMagically infused§r - Extra 5% chance to not consume durability with unbreaking."), false);
            } else if (level == 50) {
                player.sendMessage(new LiteralText("§aMiners sight§r - Night vision and haste below y40"), false);
            }
        }
    }
}
