/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public class BowArrowMixin {
    @ModifyReturnValue(method = "getProjectileType", at = @At(value = "RETURN", ordinal = 3))
    public ItemStack rpgstats$modifyGetArrow(ItemStack original) {
        //noinspection ConstantConditions
        if ((Object) this instanceof ServerPlayerEntity && original.isEmpty()) {
            if (LevelUtils.INSTANCE.getComponentLevel(Components.RANGED, (ServerPlayerEntity) (Object) this) >= 50) {
                return new ItemStack(Items.ARROW);
            }
        }
        return original;
    }
}