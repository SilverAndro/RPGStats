/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.random.RandomGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UnbreakingEnchantment.class)
public class UnbreakingEnchantmentMixin {
    @Inject(method = "shouldPreventDamage", at = @At("RETURN"), cancellable = true)
    private static void rpgstats$bonusUnbreaking(ItemStack item, int level, RandomGenerator randomGenerator, CallbackInfoReturnable<Boolean> cir) {
        if (item.getHolder() != null && item.getHolder() instanceof ServerPlayerEntity) {
            if (
                    LevelUtils.INSTANCE.getComponentLevel(Components.MINING, (ServerPlayerEntity) item.getHolder()) >= 25
                            && RPGStatsMain.levelConfig.getMining().getEnableLv25Buff()
            ) {
                if (!cir.getReturnValue() && randomGenerator.nextFloat() <= 0.05f) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}