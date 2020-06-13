package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnchantmentHelper.class)
public class ImpalingMixin {
    @Inject(
            method = "getAttackDamage",
            at = @At(
                    value = "TAIL"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void cursedManip(ItemStack stack, EntityGroup group, CallbackInfoReturnable<Float> cir, MutableFloat mutableFloat) {
        if (EnchantmentHelper.get(stack).containsKey(Enchantments.IMPALING) && !(group == EntityGroup.AQUATIC)) {
            System.out.println(stack.getHolder());
            if (RPGStats.getComponentLevel(RPGStats.RANGED_COMPONENT, ComponentProvider.fromEntity(stack.getHolder())) >= 50) {
                int level = EnchantmentHelper.get(stack).get(Enchantments.IMPALING);
                System.out.println(level);
                System.out.println(mutableFloat.getValue());
                mutableFloat.add(level * 2.5F);
                System.out.println(mutableFloat.getValue());
            }
        }
    }
}
