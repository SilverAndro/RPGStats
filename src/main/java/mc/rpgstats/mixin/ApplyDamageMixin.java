package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ApplyDamageMixin {
    private static float originalDamage = 0f;
    
    @Inject(method = "applyDamage", at = @At("HEAD"))
    public void captureOriginalDamageDealtForXpCalc(DamageSource source, float amount, CallbackInfo ci) {
        originalDamage = amount;
    }
    
    @Inject(method = "applyDamage", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;applyEnchantmentsToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F", shift = At.Shift.AFTER))
    public void grantXpFromDamageAbsorbedThroughArmorOrEnchants(DamageSource source, float amount, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object)this instanceof ServerPlayerEntity) {
            float blockedDamage = originalDamage - amount;
            if (blockedDamage <= 0) {
                return;
            }
            RPGStats.addXpAndLevelUp(
                CustomComponents.DEFENSE,
                (ServerPlayerEntity)(Object)this,
                Math.min(Math.max(1, (int)Math.floor(Math.log10(blockedDamage / 2) * 7)), 15)
            );
        }
    }
}
