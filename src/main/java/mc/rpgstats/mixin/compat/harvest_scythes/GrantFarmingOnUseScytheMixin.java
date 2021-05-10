package mc.rpgstats.mixin.compat.harvest_scythes;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wraith.harvest_scythes.ScytheTool;

@Mixin(ScytheTool.class)
public class GrantFarmingOnUseScytheMixin {
    @Inject(method = "harvest",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;",
            ordinal = 5
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void scytheCompatFarmingXP(
        int harvestRadius,
        World world,
        PlayerEntity user,
        Hand hand,
        CallbackInfoReturnable<TypedActionResult<ItemStack>> cir,
        BlockPos blockPos,
        Item item,
        int lvl,
        int radius,
        boolean circleHarvest,
        int x,
        int y,
        int z,
        BlockPos cropPos,
        BlockState blockState,
        Block block,
        int damageTool
    ) {
        if (user instanceof ServerPlayerEntity) {
            RPGStats.addXpAndLevelUp(CustomComponents.FARMING, (ServerPlayerEntity)user, damageTool);
        }
    }
}
