package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.Events;
import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class TakeDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void rpgstats$dodge(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity spe = (ServerPlayerEntity) (Object) this;
        int level = LevelUtils.INSTANCE.getComponentLevel(Components.DEFENCE, spe);
        float chance = 0f;
        if (level >= 50 && RPGStatsMain.levelConfig.defense.enableLv50Buff) {
            chance = 0.1f;
        } else if (level >= 25 && RPGStatsMain.levelConfig.defense.enableLv25Buff) {
            chance = 0.05f;
        }
        if (spe.getRandom().nextDouble() <= chance) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void rpgstats$onPlayerDeathRefreshStats(DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity le) {
            if (!le.world.isClient) {
                Events.needsStatFix.add(le);
            }
        }
    }
}