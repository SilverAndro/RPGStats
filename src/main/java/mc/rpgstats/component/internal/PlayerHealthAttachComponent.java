package mc.rpgstats.component.internal;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

public class PlayerHealthAttachComponent implements Component {
    public PlayerEntity playerEntity;
    
    public PlayerHealthAttachComponent(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }
    
    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        playerEntity
            .getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
            .setBaseValue(
                compoundTag.getDouble("max-health-saver")
            );
    }
    
    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        compoundTag.putDouble("max-health-saver",
            playerEntity
                .getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                .getValue()
        );
    }
}
