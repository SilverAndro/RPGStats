package mc.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.block.*;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Holder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BonemealMixin {
    private static final java.util.Random RANDOM = new java.util.Random();

    // TODO: Generalize this! Breaks often and not ideal
    @Inject(at = @At("HEAD"), method = "useOnGround")
    private static void rpgstats$groundEffectiveness(ItemStack stack, World world, BlockPos blockPos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (world.getBlockState(blockPos).isOf(Blocks.WATER) && world.getFluidState(blockPos).getLevel() == 8) {
            if (world instanceof ServerWorld && stack.getHolder() != null) {
                RandomGenerator random = world.getRandom();
                int level = LevelUtils.INSTANCE.getComponentLevel(CustomComponents.FARMING, (ServerPlayerEntity) stack.getHolder());

                loop:
                for (int i = 0; i < level; ++i) {
                    BlockPos blockPos2 = blockPos;
                    BlockState blockState = Blocks.SEAGRASS.getDefaultState();

                    for (int j = 0; j < i / Integer.max((level + 1) / 50, 1); ++j) {
                        blockPos2 = blockPos2.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                        if (world.getBlockState(blockPos2).isFullCube(world, blockPos2)) {
                            continue loop;
                        }
                    }

                    Holder<Biome> j = world.getBiome(blockPos2);
                    if (j.isRegistryKey(BiomeKeys.WARM_OCEAN)) {
                        if (i == 0 && facing != null && facing.getAxis().isHorizontal()) {
                            blockState = Registry.BLOCK
                                    .getTag(BlockTags.WALL_CORALS)
                                    .flatMap(blocks -> blocks.getRandomElement(world.random))
                                    .map(blockEntry -> blockEntry.value().getDefaultState())
                                    .orElse(blockState);
                            if (blockState.contains(DeadCoralWallFanBlock.FACING)) {
                                blockState = blockState.with(DeadCoralWallFanBlock.FACING, facing);
                            }
                        } else if (random.nextInt(4) == 0) {
                            blockState = Registry.BLOCK
                                    .getTag(BlockTags.UNDERWATER_BONEMEALS)
                                    .flatMap(blocks -> blocks.getRandomElement(world.random))
                                    .map(blockEntry -> blockEntry.value().getDefaultState())
                                    .orElse(blockState);
                        }
                    }

                    if (blockState.isInAndMatches(BlockTags.WALL_CORALS, state -> state.contains(DeadCoralWallFanBlock.FACING))) {
                        for (int k = 0; !blockState.canPlaceAt(world, blockPos2) && k < 4; ++k) {
                            blockState = blockState.with(DeadCoralWallFanBlock.FACING, Direction.Type.HORIZONTAL.random(random));
                        }
                    }

                    if (blockState.canPlaceAt(world, blockPos2)) {
                        BlockState k = world.getBlockState(blockPos2);
                        if (k.isOf(Blocks.WATER) && world.getFluidState(blockPos2).getLevel() == 8) {
                            world.setBlockState(blockPos2, blockState, Block.NOTIFY_ALL);
                        } else if (k.isOf(Blocks.SEAGRASS) && random.nextInt(10) == 0) {
                            ((Fertilizable) Blocks.SEAGRASS).grow((ServerWorld) world, random, blockPos2, k);
                        }
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "useOnFertilizable")
    private static void rpgstats$onGrowable(ItemStack stack, World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient && stack.getHolder() != null && stack.getHolder() instanceof ServerPlayerEntity) {
            int level = LevelUtils.INSTANCE.getComponentLevel(CustomComponents.FARMING, (ServerPlayerEntity) stack.getHolder());
            BlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof Fertilizable fertilizable) {
                if (fertilizable.isFertilizable(world, pos, blockState, false)) {
                    if (world instanceof ServerWorld) {
                        if (fertilizable.canGrow(world, world.random, pos, blockState)) {
                            if (RANDOM.nextDouble() < level * 0.03) {
                                fertilizable.grow((ServerWorld) world, world.random, pos, blockState);
                            }
                        }
                    }
                }
            }
        }
    }
}
