/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
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
                int level = LevelUtils.INSTANCE.getComponentLevel(Components.MAGIC, (ServerPlayerEntity) livingEntity);
                if (level < 25 || !RPGStatsMain.levelConfig.magic.enableLv25Buff) {
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
                int level = LevelUtils.INSTANCE.getComponentLevel(Components.MAGIC, (ServerPlayerEntity) livingEntity);
                if (level < 50 || !RPGStatsMain.levelConfig.magic.enableLv50Buff) {
                    return livingEntity.damage(source, amount);
                }
            } else {
                return livingEntity.damage(source, amount);
            }
        }
        return false;
    }
}