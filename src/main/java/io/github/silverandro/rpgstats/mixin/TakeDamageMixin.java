/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class TakeDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void rpgstats$dodge(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity spe = (ServerPlayerEntity) (Object) this;
        int level = LevelUtils.INSTANCE.getComponentLevel(Components.DEFENCE, spe);
        float chance = 0f;
        if (level >= 50 && RPGStatsMain.levelConfig.getDefense().getEnableLv50Buff()) {
            chance = 0.1f;
        } else if (level >= 25 && RPGStatsMain.levelConfig.getDefense().getEnableLv25Buff()) {
            chance = 0.05f;
        }
        if (spe.getRandom().nextDouble() <= chance) {
            cir.setReturnValue(false);
        }
    }
}