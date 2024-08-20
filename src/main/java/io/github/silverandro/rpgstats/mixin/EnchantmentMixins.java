package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixins {
    @Shadow public abstract boolean equals(Object par1);

    @Inject(method = "modifyItemDamage", at = @At("HEAD"))
    private void rpgstats$extraUnbreaking(ServerWorld world, int level, ItemStack stack, MutableFloat itemDamage, CallbackInfo ci) {
        //noinspection EqualsBetweenInconvertibleTypes
        if (this.equals(world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).get(Enchantments.UNBREAKING))) {
            if (stack.getHolder() instanceof ServerPlayerEntity player) {
                if (
                    LevelUtils.INSTANCE.getComponentLevel(Components.MINING, player) >= 25 &&
                    RPGStatsMain.levelConfig.getMining().getEnableLv25Buff()
                ) {
                    if (world.random.nextFloat() <= 0.05f) {
                        itemDamage.setValue(0f);
                    }
                }
            }
        }
    }

    @Redirect(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/effect/EnchantmentEffectEntry;test(Lnet/minecraft/loot/context/LootContext;)Z"))
    private static boolean rpgstats$universalImpaling(EnchantmentEffectEntry instance, LootContext context) {
        if (instance.equals(context.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).get(Enchantments.IMPALING).effects().get(EnchantmentEffectComponentTypes.DAMAGE).get(0))) {
            Entity attacker = context.get(LootContextParameters.ATTACKING_ENTITY);

            if (attacker instanceof ServerPlayerEntity player) {
                if (
                    LevelUtils.INSTANCE.getComponentLevel(Components.RANGED, player) >= 25
                    && RPGStatsMain.levelConfig.getRanged().getEnableLv25Buff()
                ) {
                    return true;
                }
            }
        }
        return instance.test(context);
    }
}
