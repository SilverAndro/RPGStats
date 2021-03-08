package mc.rpgstats.mixin.compat.harvest;

import info.tehnut.harvest.Harvest;
import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Harvest.class)
public class GrantFarmingOnSimpleHarvest {
    @Inject(
        remap = false,
        method = "lambda$onInitialize$3(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
        at = @At("TAIL")
    )
    private static void giveFarmingXpOnSimpleHarvest(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (player instanceof ServerPlayerEntity) {
            RPGStats.addXpAndLevelUp(CustomComponents.FARMING_COMPONENT, (ServerPlayerEntity)player, 1);
        }
    }
}
