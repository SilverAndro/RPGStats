/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
class BowAccuracyMixin {
    @Unique
    private static Entity spreadUser;

    @Inject(method = "getProjectileSpread", at = @At("HEAD"))
    private static void captureSpreadUser(ServerWorld world, ItemStack stack, Entity user, float baseProjectileSpread, CallbackInfoReturnable<Float> cir) {
        spreadUser = user;
    }

    @ModifyVariable(method = "getProjectileSpread", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableFloat;<init>(F)V"), argsOnly = true)
    private static float modifySpread(float baseSpread) {
        if (spreadUser instanceof ServerPlayerEntity player) {
            spreadUser = null;

            float mult = Math.max(1.0f - LevelUtils.INSTANCE.getComponentLevel(Components.RANGED, player) / 60f, 0);
            return baseSpread * mult;
        }

        spreadUser = null;
        return baseSpread;
    }
}