package mc.rpgstats.mixin.compat.artifality;

import io.github.silverandro.rpgstats.LevelUtils;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "artifality.item.HarvestStaffItem")
public abstract class GrantFarmingOnStaffHarvest extends Item {
    public GrantFarmingOnStaffHarvest(Settings settings) {
        super(settings);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private void rpgstats$giveFarmingXpOnStaffHarvest(ItemUsageContext crop, CallbackInfoReturnable<ActionResult> cir) {
        if (crop.getPlayer() instanceof ServerPlayerEntity player) {
            LevelUtils.INSTANCE.addXpAndLevelUp(CustomComponents.FARMING, player, 1);
        }
    }
}
