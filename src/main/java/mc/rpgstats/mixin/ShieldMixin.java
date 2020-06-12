package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ShieldMixin {
    @Inject(at = @At("HEAD"), method = "damageShield")
    private void onShieldUse(float amount, CallbackInfo ci) {
        RPGStats.addXpAndLevelUpIfNeeded(RPGStats.DEFENCE_COMPONENT, ComponentProvider.fromEntity((PlayerEntity)(Object)this), (int)Math.floor(amount / 3));
    }
}
