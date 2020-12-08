package mc.rpgstats.component;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
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
    public String getCapName() {
        return "Ranged";
    }
    
    @Override
    public void onLevelUp(boolean beQuiet) {
        if (!beQuiet) {
            player.sendMessage(new LiteralText("§a+1§r Bow accuracy"), false);
            
            if (level == 25) {
                player.sendMessage(new LiteralText("§aAqueus§r - Impaling applies to all mobs, not just water based ones"), false);
            } else if (level == 50) {
                player.sendMessage(new LiteralText("§aNix§r - You no longer need arrows"), false);
            }
        }
    }
}
