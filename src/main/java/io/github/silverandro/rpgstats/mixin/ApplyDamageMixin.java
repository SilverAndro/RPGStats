/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ApplyDamageMixin {
    @Unique
    private static final TagKey<DamageType> DAMAGE_XP_BLACKLIST = TagKey.of(
        RegistryKeys.DAMAGE_TYPE,
        new Identifier("rpgstats", "damage_blacklist")
    );
    @Unique
    private static float originalDamage = 0f;

    @Inject(method = "applyDamage", at = @At("HEAD"))
    public void rpgstats$captureOriginalDamageDealtForXpCalc(DamageSource source, float amount, CallbackInfo ci) {
        originalDamage = amount;
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;applyEnchantmentsToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F", shift = At.Shift.AFTER))
    public void rpgstats$grantXpFromDamageAbsorbedThroughArmorOrEnchants(DamageSource source, float amount, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof ServerPlayerEntity && sourceCanGrantXp(source)) {
            float blockedDamage = originalDamage - amount;
            if (blockedDamage <= 3) {
                return;
            }
            LevelUtils.INSTANCE.addXpAndLevelUp(
                    Components.DEFENCE,
                    (ServerPlayerEntity) (Object) this,
                    Math.min((int) Math.floor(Math.log(Math.pow(blockedDamage, 5.0f))), 4));
        }
    }

    @Unique
    public boolean sourceCanGrantXp(DamageSource source) {
        return !source.isTypeIn(DAMAGE_XP_BLACKLIST);
    }
}