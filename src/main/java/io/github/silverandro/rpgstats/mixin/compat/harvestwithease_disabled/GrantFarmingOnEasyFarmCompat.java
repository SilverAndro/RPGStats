/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin.compat.harvestwithease_disabled;

import io.github.silverandro.rpgstats.EventsKt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = { "crystalspider.harvestwithease_disabled.handlers.UseBlockHandler" })
public class GrantFarmingOnEasyFarmCompat {
    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Lcrystalspider/harvestwithease_disabled/handlers/UseBlockHandler;grantExp(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void rpgstats$grantXpOnHarvestWithEase(PlayerEntity player, World world, Hand hand, BlockHitResult result, CallbackInfoReturnable<ActionResult> cir) {
        EventsKt.grantBlockBreakXP(world, player, result.getBlockPos(), world.getBlockState(result.getBlockPos()));
    }
}
