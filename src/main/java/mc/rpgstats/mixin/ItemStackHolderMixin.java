package mc.rpgstats.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public abstract class ItemStackHolderMixin {
    @Shadow public abstract void setHolder(Entity holder);

    @Shadow public abstract Entity getHolder();

    @Inject(at = @At("HEAD"), method = "inventoryTick")
    private void onTick(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        setHolder(entity);
    }

    @Inject(at = @At("TAIL"), method = "copy", locals = LocalCapture.CAPTURE_FAILHARD)
    private void copyHolder(CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack) {
        itemStack.setHolder(this.getHolder());
    }
}