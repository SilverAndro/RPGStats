package io.github.silverandro.rpgstats.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Consumer;

/*
if (EnchantmentHelper.get(stack).containsKey(Enchantments.IMPALING) && !(group == EntityGroup.AQUATIC)) {
    if (stack.getHolder() != null && stack.getHolder() instanceof ServerPlayerEntity) {
        if (
                LevelUtils.INSTANCE.getComponentLevel(Components.RANGED, (ServerPlayerEntity) stack.getHolder()) >= 25
                        && RPGStatsMain.levelConfig.getRanged().getEnableLv25Buff()
        ) {
            int level = EnchantmentHelper.get(stack).get(Enchantments.IMPALING);
            mutableFloat.add(level * 2.5F);
        }
    }
}
 */

@Mixin(Enchantment.class)
public class UnbreakingMixin {
    @Inject(method = "applyEffects", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"), locals = LocalCapture.PRINT)
    private static <T> void rpgstats$extraUnbreaking(List<EnchantmentEffectEntry<T>> entries, LootContext lootContext, Consumer<T> effectConsumer, CallbackInfo ci) {
        /*
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
        */
    }
}
