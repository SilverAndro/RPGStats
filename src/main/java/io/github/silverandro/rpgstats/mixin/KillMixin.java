/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin;

import com.mojang.datafixers.util.Either;
import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.datadrive.xp.XpData;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public abstract class KillMixin {
    @Inject(at = @At("HEAD"), method = "onDeath")
    private void rpgstats$onKilledXPTracker(DamageSource source, CallbackInfo info) {
        LivingEntity le = (LivingEntity) (Object) this;
        if (!le.world.isClient && !le.isRemoved()) {
            Entity entity = source.getAttacker();

            if (entity instanceof ServerPlayerEntity serverPlayer) {
                Either<XpData.XpEntry, List<XpData.XpEntry>> reaOverride = XpData.INSTANCE.getENTITY_XP_OVERRIDE().get(le.getType()).orElse(null);
                if (reaOverride != null) {
                    reaOverride.ifLeft(xpEntry -> LevelUtils.INSTANCE.applyReaEntry(xpEntry, serverPlayer, source));
                    reaOverride.ifRight(xpEntries -> xpEntries.forEach(xpEntry -> LevelUtils.INSTANCE.applyReaEntry(xpEntry, serverPlayer, source)));
                    return;
                }

                if (source.isType(DamageTypes.MAGIC)) {
                    LevelUtils.INSTANCE.addXpAndLevelUp(Components.MAGIC, serverPlayer, 1);
                } else if (source.isIndirect()) {
                    if (le instanceof WitherEntity || le instanceof EnderDragonEntity) {
                        LevelUtils.INSTANCE.addXpAndLevelUp(Components.RANGED, serverPlayer, 130);
                    } else {
                        LevelUtils.INSTANCE.addXpAndLevelUp(Components.RANGED, serverPlayer, 1);
                    }
                } else if (!source.isType(DamageTypes.EXPLOSION) && !source.isType(DamageTypes.ON_FIRE) && !source.isType(DamageTypes.IN_FIRE)) {
                    if (le instanceof PassiveEntity) {
                        LevelUtils.INSTANCE.addXpAndLevelUp(Components.FARMING, serverPlayer, 1);
                    } else {
                        int level = LevelUtils.INSTANCE.getComponentLevel(Components.MELEE, serverPlayer);

                        int duration = 0;
                        if (level >= 50 && RPGStatsMain.levelConfig.melee.enableLv50Buff) {
                            duration = 200;
                        } else if (level >= 25 && RPGStatsMain.levelConfig.melee.enableLv25Buff) {
                            duration = 100;
                        }

                        if (duration > 0) {
                            ((ServerPlayerEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration));
                        }

                        LevelUtils.INSTANCE.addXpAndLevelUp(Components.MELEE, serverPlayer, 1);
                    }
                }
            }
        }
    }
}
