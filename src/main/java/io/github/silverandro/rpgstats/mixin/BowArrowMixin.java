package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.stats.Components;
import io.github.silverandro.rpgstats.main.RPGStats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public class BowArrowMixin {
    private ServerPlayerEntity itemUser = null;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;creativeMode:Z", ordinal = 0, shift = At.Shift.BY, by = -2), method = "onStoppedUsing")
    public void rpgstats$capturePlayerUsingBow(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof ServerPlayerEntity) {
            itemUser = (ServerPlayerEntity) user;
        }
    }

    @ModifyVariable(method = "onStoppedUsing", at = @At(value = "INVOKE_ASSIGN", ordinal = 2, shift = At.Shift.AFTER), ordinal = 0)
    public boolean rpgstats$forceCanShootArrow(boolean bl) {
        if (
                itemUser != null
                        && LevelUtils.INSTANCE.getComponentLevel(Components.RANGED, itemUser) >= 50
                        && RPGStats.getConfig().toggles.ranged.enableLv50Buff
        ) {
            itemUser = null;
            return true;
        }
        return bl;
    }
}
