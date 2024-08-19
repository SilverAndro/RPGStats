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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

// Have to lower priority due to mixin bug with mixin conflict handling
@Mixin(value = PotionItem.class, priority = 900)
public class PotionDrinkMixin {
    // What is this bruh. None of the capturing is documented, and it's a core feature of the annotation
    // Also it literally says Redirect is better :rolling_eyes:
    @ModifyArgs(
            method = "method_57389(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/effect/StatusEffectInstance;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"
            )
    )
    private static void rpgstats$OnFinishDrinkingPotion(Args args) {
        for (int i = 0; i < args.size(); i++) {
            System.out.println(String.valueOf(args.get(i)));
        }

        // Yay! no type safety
        StatusEffectInstance effect = args.get(0);

        if (null instanceof ServerPlayerEntity playerEntity) {
            LevelUtils.INSTANCE.addXpAndLevelUp(Components.MAGIC, playerEntity, 10);

            int newDuration;
            if (LevelUtils.INSTANCE.getComponentLevel(Components.MAGIC, playerEntity) > 0) {
                newDuration = effect.getDuration() + (effect.getDuration() / ((RPGStatsMain.config.getScaling().getMaxLevel() * 5) / LevelUtils.INSTANCE.getComponentLevel(Components.MAGIC, playerEntity)));
            } else {
                newDuration = effect.getDuration();
            }

            // Why is `permanent` mutable but not anything else
            StatusEffectInstance newInstance = new StatusEffectInstance(
                    effect.getEffectType(),
                    newDuration,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.shouldShowParticles(),
                    effect.shouldShowIcon()
            );

            args.set(0, newInstance);
        }
    }

    @Inject(
            method = "finishUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/stat/Stat;)V"
            )
    )
    private void rpgstats$OnFinishDrinkingHealthPotion(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof ServerPlayerEntity playerEntity) {
            LevelUtils.INSTANCE.addXpAndLevelUp(Components.MAGIC, playerEntity, 10);

        }
    }

    @Inject(at = @At("HEAD"), method = "getMaxUseTime", cancellable = true)
    private void rpgstats$getPotionUseTime(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (user instanceof ServerPlayerEntity holder) {
            cir.setReturnValue((int) (32 - Math.floor(LevelUtils.INSTANCE.getComponentLevel(Components.MAGIC, holder) / 3.0f)));
        }
    }
}