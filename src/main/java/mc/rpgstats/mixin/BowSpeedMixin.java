package mc.rpgstats.mixin;

import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowSpeedMixin {
    @Inject(method = "getMaxUseTime", at = @At(value = "HEAD"), cancellable = true)
    public void changeMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        System.out.println(stack.getHolder());
        cir.setReturnValue(100);
    }
}
