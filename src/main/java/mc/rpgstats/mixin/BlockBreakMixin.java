package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Block.class)
public class BlockBreakMixin {
    @Inject(at = @At("HEAD"), method = "onBreak")
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (!world.isClient) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            Block block = state.getBlock();
            if (block instanceof PlantBlock || block instanceof PumpkinBlock || block instanceof MelonBlock || block instanceof CocoaBlock) {
                if (block instanceof CropBlock) {
                    if (((CropBlock)block).isMature(state)) {
                        RPGStats.addXpAndLevelUp(CustomComponents.FARMING, serverPlayer, 1);
                    }
                } else {
                    RPGStats.addXpAndLevelUp(CustomComponents.FARMING, serverPlayer, 1);
                }
            }
    
            Random random = new Random();
            if (block instanceof OreBlock && random.nextBoolean()) {
                int amount;
                if (
                    block == Blocks.COAL_ORE ||
                        block == Blocks.NETHER_GOLD_ORE
                ) {
                    amount = 1;
                } else if (
                    block == Blocks.IRON_ORE ||
                        block == Blocks.NETHER_QUARTZ_ORE
                ) {
                    amount = 2;
                } else if (
                    block == Blocks.GOLD_ORE ||
                        block == Blocks.LAPIS_ORE ||
                        block == Blocks.REDSTONE_ORE
                ) {
                    amount = 3;
                } else if (block == Blocks.EMERALD_ORE) {
                    amount = 4;
                } else if (
                    block == Blocks.DIAMOND_ORE ||
                        block == Blocks.ANCIENT_DEBRIS
                ) {
                    amount = 5;
                } else {
                    amount = 2;
                }
                RPGStats.addXpAndLevelUp(CustomComponents.MINING, serverPlayer, amount);
            }
        }
    }
}
