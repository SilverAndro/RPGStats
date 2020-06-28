package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
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
            if (RPGStats.getComponentLevel(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(item.getHolder())) >= 25) {
                if (!cir.getReturnValue() && random.nextFloat() <= 0.05f) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
