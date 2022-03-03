package mc.rpgstats.mixin.compat.artifality;

import artifality.item.HarvestStaffItem;
import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(HarvestStaffItem.class)
public class GrantFarmingOnStaffHarvest {
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private void rpgstats$giveFarmingXpOnStaffHarvest(ItemUsageContext crop, CallbackInfoReturnable<ActionResult> cir){
        if (crop.getPlayer() instanceof ServerPlayerEntity player) {
            RPGStats.addXpAndLevelUp(CustomComponents.FARMING, player, 1);
        }
    }
}