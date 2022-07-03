package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mixin(LivingEntity.class)
public class ItemEatMixin {
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "applyFoodEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isFood()Z"))
    public void rpgstats$grantFishEffects(ItemStack stack, World world, LivingEntity targetEntity, CallbackInfo ci) {
        LivingEntity le = (LivingEntity) (Object) this;
        if (le instanceof ServerPlayerEntity) {
            int level = LevelUtils.INSTANCE.getComponentLevel(Components.FISHING, (ServerPlayerEntity) le);
            if (level >= 25 && stack.isIn(ItemTags.FISHES) && RPGStatsMain.levelConfig.fishing.enableLv25Buff) {
                List<StatusEffect> goodEffects = Arrays.asList(
                        StatusEffects.ABSORPTION,
                        StatusEffects.CONDUIT_POWER,
                        StatusEffects.DOLPHINS_GRACE,
                        StatusEffects.FIRE_RESISTANCE,
                        StatusEffects.HASTE,
                        StatusEffects.HEALTH_BOOST,
                        StatusEffects.HERO_OF_THE_VILLAGE,
                        StatusEffects.INSTANT_HEALTH,
                        StatusEffects.JUMP_BOOST,
                        StatusEffects.LUCK,
                        StatusEffects.NIGHT_VISION,
                        StatusEffects.REGENERATION,
                        StatusEffects.RESISTANCE,
                        StatusEffects.SPEED,
                        StatusEffects.STRENGTH,
                        StatusEffects.WATER_BREATHING
                );

                // im lazy
                Collections.shuffle(goodEffects);

                le.addStatusEffect(new StatusEffectInstance(goodEffects.get(0), 30 * 20, 0));
            }

            if (level >= 50 && RPGStatsMain.levelConfig.fishing.enableLv50Buff) {
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 1, 0));
            }
        }
    }
}
