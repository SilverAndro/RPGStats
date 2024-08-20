package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.effect.PoisonStatusEffect")
public class PoisonImmuneMixin {
    @Inject(method = "applyUpdateEffect", at = @At("HEAD"), cancellable = true)
    public void rpgstats$negatePoison(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        if (!entity.getWorld().isClient) {
            if (entity instanceof ServerPlayerEntity serverPlayer) {
                int level = LevelUtils.INSTANCE.getComponentLevel(Components.MAGIC, serverPlayer);
                if (level > 25 && !RPGStatsMain.levelConfig.getMagic().getEnableLv25Buff()) {
                    cir.setReturnValue(false);
                    cir.cancel();
                }
            }
        }
    }
}
