package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockBreakMixin {
    @Inject(at = @At("HEAD"), method = "onBreak")
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (!world.isClient) {
            Block block = state.getBlock();
            if (block instanceof PlantBlock || block instanceof PumpkinBlock || block instanceof MelonBlock) {
                if (block instanceof CropBlock) {
                    if (((CropBlock)block).isMature(state)) {
                        RPGStats.addXpAndLevelUpIfNeeded(RPGStats.FARMING_COMPONENT, ComponentProvider.fromEntity(player), 1);
                    }
                } else {
                    RPGStats.addXpAndLevelUpIfNeeded(RPGStats.FARMING_COMPONENT, ComponentProvider.fromEntity(player), 1);
                }
            }
            
            if (block instanceof OreBlock) {
                if (
                    block == Blocks.COAL_ORE ||
                        block == Blocks.NETHER_GOLD_ORE
                ) {
                    RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(player), 1);
                } else if (
                    block == Blocks.IRON_ORE ||
                        block == Blocks.NETHER_QUARTZ_ORE
                ) {
                    RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(player), 2);
                } else if (
                    block == Blocks.GOLD_ORE ||
                        block == Blocks.LAPIS_ORE ||
                        block == Blocks.REDSTONE_ORE
                ) {
                    RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(player), 3);
                } else if (block == Blocks.EMERALD_ORE) {
                    RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(player), 4);
                } else if (
                    block == Blocks.DIAMOND_ORE ||
                        block == Blocks.ANCIENT_DEBRIS
                ) {
                    RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(player), 5);
                } else {
                    RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(player), 2);
                }
            }
        }
    }
}
