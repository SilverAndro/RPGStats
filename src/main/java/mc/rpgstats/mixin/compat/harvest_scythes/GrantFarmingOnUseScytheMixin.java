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
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wraith.harvest_scythes.ScytheTool;

@Pseudo
@Mixin(ScytheTool.class)
public class GrantFarmingOnUseScytheMixin {
    @Inject(method = "harvest",
        at = @At(value = "RETURN",
            ordinal = 0
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
        ItemStack stack,
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
