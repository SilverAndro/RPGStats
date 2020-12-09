package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.StatComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public class BowArrowMixin {
    private ServerPlayerEntity itemUser = null;
    
    @Inject(at = @At(value = "HEAD"), method = "onStoppedUsing", cancellable = true)
    public void capturePlayerUsingBow(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof ServerPlayerEntity) {
            itemUser = (ServerPlayerEntity)user;
        }
    }
    
    @ModifyVariable(method = "onStoppedUsing", at = @At("INVOKE_ASSIGN"), name = "bl")
    public boolean createArrowIfHasNix(boolean bl) {
        if (RPGStats.getComponentLevel(StatComponents.RANGED_COMPONENT, itemUser) >= 50) {
            return true;
        }
        return bl;
    }
}
