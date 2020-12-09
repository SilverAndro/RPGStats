package mc.rpgstats.component;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;

public class MagicComponent implements IStatComponent {
    private final PlayerEntity player;
    private int xp = 0;
    private int level = 0;
    
    public MagicComponent(PlayerEntity player) {
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
        return "magic";
    }
    
    @Override
    public String getCapName() {
        return "Magic";
    }
    
    @Override
    public void onLevelUp(boolean beQuiet) {
        if (!beQuiet) {
            player.sendMessage(new LiteralText("§a+1§r Drunk potion duration"), false);
            
            if (level % 3 == 0) {
                player.sendMessage(new LiteralText("§a+1§r Potion drink speed"), false);
            }
            
            if (level == 25) {
                player.sendMessage(new LiteralText("§aVax§r - Immune to poison"), false);
            } else if (level == 50) {
                player.sendMessage(new LiteralText("§aDead inside§r - Immune to wither"), false);
            }
        }
    }
}
