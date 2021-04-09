package mc.rpgstats.component.internal;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

public class PlayerHealthAttachComponent implements Component {
    public PlayerEntity playerEntity;
    
    public double amount = 0.0;
    
    public PlayerHealthAttachComponent(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }
    
    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        amount = compoundTag.getDouble("max-health-mod");
        playerEntity
            .getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
            .setBaseValue(
                playerEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).getValue() + amount
            );
    }
    
    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        compoundTag.putDouble("max-health-mod",
            amount
        );
    }
}
