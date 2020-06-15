package mc.rpgstats.components;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import java.util.Objects;

public class MeleeComponent implements IStatComponent {
    private final PlayerEntity player;
    private int xp = 0;
    private int level = 0;

    public MeleeComponent(PlayerEntity player) {
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
        return RPGStats.MELEE_COMPONENT;
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
        return "melee";
    }

    @Override
    public String getCapName() {
        return "Melee";
    }

    @Override
    public void onLevelUp() {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + 0.1);
        player.sendMessage(new LiteralText("§a+0.12§r Melee damage"), false);

        if (level == 25) {
            player.sendMessage(new LiteralText("§aBloodthirst§r - Regain 1 heart after killing a monster"), false);
        } else if (level == 50) {
            player.sendMessage(new LiteralText("§aBloodthirst II§r - Regain 2 hearts after killing a monster"), false);
        }
    }
}
