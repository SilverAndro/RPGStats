package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class BowCanUseMixin {
    @Inject(method = "getArrowType", at = @At("HEAD"), cancellable = true)
    public void modifyGetArrow(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        //noinspection ConstantConditions
        if ((Object)this instanceof ServerPlayerEntity) {
            if (RPGStats.getComponentLevel(CustomComponents.RANGED_COMPONENT.getId(), (ServerPlayerEntity)(Object)this) >= 50) {
                cir.setReturnValue(new ItemStack(Items.ARROW));
            }
        }
    }
}
