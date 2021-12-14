package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class BlockBreakSpeedMixin {
    @ModifyVariable(at = @At(value = "TAIL", shift = At.Shift.BEFORE), method = "getBlockBreakingSpeed", ordinal = 0)
    public float modifyFinalMiningSpeed(float f) {
        PlayerEntity player = (PlayerEntity)(Object)this;
    
        if (CustomComponents.components.containsKey(CustomComponents.MINING)) {
            int level = CustomComponents.STATS.get(player).getOrCreateID(CustomComponents.MINING).getLevel();
            System.out.println("Was " + f + " is now " + (f + (level * 0.1f)));
            return (f + (level * 0.1f));
        }
        return f;
    }
}
