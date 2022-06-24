package mc.rpgstats.mixin.compat.harvest;

import info.tehnut.harvest.Harvest;
import io.github.silverandro.rpgstats.LevelUtils;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(Harvest.class)
public class GrantFarmingOnSimpleHarvest {
    @Inject(
        method = "lambda$onInitialize$3",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V")
    )
    private static void rpgstats$giveFarmingXpOnSimpleHarvest(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (player instanceof ServerPlayerEntity) {
            LevelUtils.INSTANCE.addXpAndLevelUp(CustomComponents.FARMING, (ServerPlayerEntity)player, 1);
        }
    }
}