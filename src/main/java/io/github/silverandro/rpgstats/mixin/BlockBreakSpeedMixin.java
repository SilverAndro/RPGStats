package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class BlockBreakSpeedMixin {
    @ModifyVariable(at = @At(value = "TAIL", shift = At.Shift.BEFORE), method = "getBlockBreakingSpeed", ordinal = 0)
    public float rpgstats$modifyFinalMiningSpeed(float f) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (Components.components.containsKey(Components.MINING)) {
            int level = Components.STATS.get(player).getOrCreateID(Components.MINING).getLevel();
            return (f + (level * 0.1f));
        }
        return f;
    }
}