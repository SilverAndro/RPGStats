package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Mixin(BoneMealItem.class)
public class BonemealMixin {
    private static final Random RANDOM = new Random();

    @Inject(at = @At("HEAD"), method = "useOnGround")
    private static void groundEffectiveness(ItemStack stack, World world, BlockPos blockPos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient && world.getBlockState(blockPos).isOf(Blocks.WATER) && world.getFluidState(blockPos).getLevel() == 8) {
            if (world instanceof ServerWorld && stack.getHolder() != null && stack.getHolder() instanceof ServerPlayerEntity) {
                int level = RPGStats.getComponentLevel(CustomComponents.FARMING, (ServerPlayerEntity)stack.getHolder());
                cursedLoop:
                for (int i = 0; i < level; ++i) {
                    BlockPos blockPos2 = blockPos;
                    BlockState blockState = Blocks.SEAGRASS.getDefaultState();

                    int k;
                    for (k = 0; k < i / ((level + 1) / 50); ++k) {
                        blockPos2 = blockPos2.add(RANDOM.nextInt(3) - 1, (RANDOM.nextInt(3) - 1) * RANDOM.nextInt(3) / 2, RANDOM.nextInt(3) - 1);
                        if (world.getBlockState(blockPos2).isFullCube(world, blockPos2)) {
                            continue cursedLoop;
                        }
                    }
    
                    Optional<RegistryKey<Biome>> optional = world.getBiomeKey(blockPos2);
                    if (Objects.equals(optional, Optional.of(BiomeKeys.WARM_OCEAN)) || Objects.equals(optional, Optional.of(BiomeKeys.DEEP_WARM_OCEAN))) {
                        if (i == 0 && facing != null && facing.getAxis().isHorizontal()) {
                            blockState = BlockTags.WALL_CORALS.getRandom(world.random).getDefaultState().with(DeadCoralWallFanBlock.FACING, facing);
                        } else if (RANDOM.nextInt(4) == 0) {
                            blockState = BlockTags.UNDERWATER_BONEMEALS.getRandom(RANDOM).getDefaultState();
                        }
                    }

                    if (BlockTags.WALL_CORALS.contains(blockState.getBlock())) {
                        for (k = 0; !blockState.canPlaceAt(world, blockPos2) && k < 4; ++k) {
                            blockState = blockState.with(DeadCoralWallFanBlock.FACING, Direction.Type.HORIZONTAL.random(RANDOM));
                        }
                    }

                    if (blockState.canPlaceAt(world, blockPos2)) {
                        BlockState blockState2 = world.getBlockState(blockPos2);
                        if (blockState2.isOf(Blocks.WATER) && world.getFluidState(blockPos2).getLevel() == 8) {
                            world.setBlockState(blockPos2, blockState, 3);
                        } else if (blockState2.isOf(Blocks.SEAGRASS) && RANDOM.nextInt(10) == 0) {
                            ((Fertilizable) Blocks.SEAGRASS).grow((ServerWorld) world, RANDOM, blockPos2, blockState2);
                        }
                    }
                }
                stack.decrement(1);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "useOnFertilizable")
    private static void onGrowable(ItemStack stack, World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient && stack.getHolder() != null && stack.getHolder() instanceof ServerPlayerEntity) {
            int level = RPGStats.getComponentLevel(CustomComponents.FARMING, (ServerPlayerEntity)stack.getHolder());
            BlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof Fertilizable) {
                Fertilizable fertilizable = (Fertilizable)blockState.getBlock();
                if (fertilizable.isFertilizable(world, pos, blockState, false)) {
                    if (world instanceof ServerWorld) {
                        if (fertilizable.canGrow(world, world.random, pos, blockState)) {
                            if (RANDOM.nextDouble() < level * 0.1) {
                                fertilizable.grow((ServerWorld)world, world.random, pos, blockState);
                            }
                        }
                        stack.decrement(1);
                    }
                }
            }
        }
    }
}
