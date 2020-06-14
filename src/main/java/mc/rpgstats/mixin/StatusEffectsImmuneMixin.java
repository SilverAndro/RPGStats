package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
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
    public boolean negatePoison(LivingEntity livingEntity, DamageSource source, float amount) {
        if (livingEntity instanceof ServerPlayerEntity) {
            int level = RPGStats.getComponentLevel(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(livingEntity));
            if (level < 25) {
                return livingEntity.damage(source, amount);
            }
        }
        return false;
    }

    @Redirect(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", ordinal = 1))
    public boolean negateWither(LivingEntity livingEntity, DamageSource source, float amount) {
        if (livingEntity instanceof ServerPlayerEntity) {
            int level = RPGStats.getComponentLevel(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(livingEntity));
            if (level < 50) {
                return livingEntity.damage(source, amount);
            }
        }
        return false;
    }
}
