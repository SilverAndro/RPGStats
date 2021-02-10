package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(UnbreakingEnchantment.class)
public class UnbreakingEnchantmentMixin {
    @Inject(method = "shouldPreventDamage", at = @At("RETURN"), cancellable = true)
    private static void bonusUnbreaking(ItemStack item, int level, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (item.getHolder() != null && item.getHolder() instanceof ServerPlayerEntity) {
            if (
                RPGStats.getComponentLevel(CustomComponents.MINING_COMPONENT, (ServerPlayerEntity)item.getHolder()) >= 25
                && RPGStats.getConfig().toggles.mining.enableLv25Buff
            ) {
                if (!cir.getReturnValue() && random.nextFloat() <= 0.05f) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
