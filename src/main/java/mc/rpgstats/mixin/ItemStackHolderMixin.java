package mc.rpgstats.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackHolderMixin {
    @Shadow public abstract void setHolder(Entity holder);

    @Inject(at = @At("HEAD"), method = "inventoryTick")
    private void onTick(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        setHolder(entity);
    }
}