package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_8635")
public class PoisonImmuneMixin {
    @Inject(method = "applyUpdateEffect", at = @At("HEAD"), cancellable = true)
    public void rpgstats$negatePoison(LivingEntity entity, int amplifier, CallbackInfo ci) {
        if (!entity.getWorld().isClient) {
            if (entity instanceof ServerPlayerEntity serverPlayer) {
                int level = LevelUtils.INSTANCE.getComponentLevel(Components.MAGIC, serverPlayer);
                if (level > 25 && !RPGStatsMain.levelConfig.getMagic().getEnableLv25Buff()) {
                    ci.cancel();
                }
            }
        }
    }
}
