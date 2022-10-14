package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffect.class)
public class StatusEffectsImmuneMixin {
    private static LivingEntity rpgstats$leEffectCapture;

    @Inject(
        method = "applyUpdateEffect",
        at = @At("HEAD")
    )
    public void rpgstats$captureLe(LivingEntity entity, int amplifier, CallbackInfo ci) {
        rpgstats$leEffectCapture = entity;
    }

    @ModifyArg(
        method = "applyUpdateEffect",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            ordinal = 0
        ),
        index = 1
    )
    public float rpgstats$negatePoison(DamageSource source, float amount) {
        if (!rpgstats$leEffectCapture.world.isClient) {
            if (rpgstats$leEffectCapture instanceof ServerPlayerEntity player) {
                int level = RPGStats.getComponentLevel(CustomComponents.MAGIC, player);
                if (level < 25 || !RPGStats.getConfig().toggles.magic.enableLv25Buff) {
                    return amount;
                } else {
                    return 0f;
                }
            }
        }

        return amount;
    }

    @ModifyArg(
            method = "applyUpdateEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 1
            ),
            index = 1
    )
    public float rpgstats$negateWither(DamageSource source, float amount) {
        if (!rpgstats$leEffectCapture.world.isClient) {
            if (rpgstats$leEffectCapture instanceof ServerPlayerEntity player) {
                int level = RPGStats.getComponentLevel(CustomComponents.MAGIC, player);
                if (level < 50 || !RPGStats.getConfig().toggles.magic.enableLv50Buff) {
                    return amount;
                } else {
                    return 0f;
                }
            }
        }

        return amount;
    }
}