package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.StatComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ShieldMixin {
    @Inject(at = @At("HEAD"), method = "damageShield")
    private void onShieldUse(float amount, CallbackInfo ci) {
        RPGStats.addXpAndLevelUp(StatComponents.DEFENSE_COMPONENT, (ServerPlayerEntity)(Object)this, Math.max(1, (int)Math.floor(amount / 2.5)));
    }
}
