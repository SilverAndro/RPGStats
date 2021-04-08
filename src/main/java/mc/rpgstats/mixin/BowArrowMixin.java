package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.CustomComponents;
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
    
    @ModifyVariable(method = "onStoppedUsing", at = @At(value = "INVOKE_ASSIGN", ordinal = 1), ordinal = 0)
    public boolean createArrowIfHasNix(boolean bl) {
        if (
            itemUser != null
            && RPGStats.getComponentLevel(CustomComponents.RANGED_COMPONENT.getId(), itemUser) >= 50
            && RPGStats.getConfig().toggles.ranged.enableLv50Buff
        ) {
            return true;
        }
        return bl;
    }
}
