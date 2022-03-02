package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StatusEffect.class)
public class StatusEffectsImmuneMixin {
    @Redirect(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", ordinal = 0))
    public boolean rpgstats$negatePoison(LivingEntity livingEntity, DamageSource source, float amount) {
        if (!livingEntity.world.isClient) {
            if (livingEntity instanceof ServerPlayerEntity) {
                int level = RPGStats.getComponentLevel(CustomComponents.MAGIC, (ServerPlayerEntity)livingEntity);
                if (level < 25 || !RPGStats.getConfig().toggles.magic.enableLv25Buff) {
                    return livingEntity.damage(source, amount);
                }
            } else {
                return livingEntity.damage(source, amount);
            }
        }
        return false;
    }

    @Redirect(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", ordinal = 1))
    public boolean rpgstats$negateWither(LivingEntity livingEntity, DamageSource source, float amount) {
        if (!livingEntity.world.isClient) {
            if (livingEntity instanceof ServerPlayerEntity) {
                int level = RPGStats.getComponentLevel(CustomComponents.MAGIC, (ServerPlayerEntity)livingEntity);
                if (level < 50 || !RPGStats.getConfig().toggles.magic.enableLv50Buff) {
                    return livingEntity.damage(source, amount);
                }
            } else {
                return livingEntity.damage(source, amount);
            }
        }
        return false;
    }
}