/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class BowCanUseMixin {
    @Inject(method = "getArrowType", at = @At("HEAD"), cancellable = true)
    public void rpgstats$modifyGetArrow(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        //noinspection ConstantConditions
        if ((Object) this instanceof ServerPlayerEntity) {
            if (LevelUtils.INSTANCE.getComponentLevel(Components.RANGED, (ServerPlayerEntity) (Object) this) >= 50) {
                cir.setReturnValue(new ItemStack(Items.ARROW));
            }
        }
    }
}