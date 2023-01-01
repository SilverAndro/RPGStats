package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionEntity.class)
public class PotionSplashMixin {
    @Inject(at = @At("HEAD"), method = "applySplashPotion")
    private void rpgstats$onSplash(List<StatusEffectInstance> statusEffects, Entity entity, CallbackInfo ci) {
        PotionEntity pe = (PotionEntity) (Object) this;
        Box box = pe.getBoundingBox().expand(4.0D, 2.0D, 4.0D);
        List<LivingEntity> list = pe.world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!list.isEmpty()) {
            for (LivingEntity le : list) {
                if (le instanceof ServerPlayerEntity) {
                    LevelUtils.INSTANCE.addXpAndLevelUp(Components.MAGIC, (ServerPlayerEntity) le, 10);
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "applyLingeringPotion")
    private void rpgstats$onLingering(ItemStack stack, Potion potion, CallbackInfo ci) {
        PotionEntity pe = (PotionEntity) (Object) this;
        Box box = pe.getBoundingBox().expand(4.0D, 2.0D, 4.0D);
        List<LivingEntity> list = pe.world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!list.isEmpty()) {
            for (LivingEntity le : list) {
                if (le instanceof ServerPlayerEntity) {
                    LevelUtils.INSTANCE.addXpAndLevelUp(Components.MAGIC, (ServerPlayerEntity) le, 10);
                }
            }
        }
    }
}