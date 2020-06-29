package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class BowArrowMixin {
    @Inject(at = @At(value = "TAIL"), method = "getArrowType", cancellable = true)
    public void noArrowRequired(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.getHolder() != null && stack.getHolder() instanceof PlayerEntity) {
            if (cir.getReturnValue().getItem() == Items.AIR && RPGStats.getComponentLevel(RPGStats.RANGED_COMPONENT, ComponentProvider.fromEntity(stack.getHolder())) >= 50) {
                cir.setReturnValue(new ItemStack(Items.ARROW));
            }
        }
    }
}
