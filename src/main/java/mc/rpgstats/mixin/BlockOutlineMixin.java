package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class BlockOutlineMixin {
    @Shadow
    private ClientWorld world;
    
    @Shadow
    private static void drawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
    
    }
    
    @Inject(method = "drawBlockOutline", at = @At("RETURN"))
    private void lavaOverlay(MatrixStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        if (
            RPGStats.getComponentLevel(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(entity)) >= 50 && (
            world.getBlockState(blockPos.add(0, 1, 0)).getBlock() == Blocks.LAVA ||
                world.getBlockState(blockPos.add(0, -1, 0)).getBlock() == Blocks.LAVA ||
                world.getBlockState(blockPos.add(0, 0, 1)).getBlock() == Blocks.LAVA ||
                world.getBlockState(blockPos.add(0, 0, -1)).getBlock() == Blocks.LAVA ||
                world.getBlockState(blockPos.add(1, 0, 0)).getBlock() == Blocks.LAVA ||
                world.getBlockState(blockPos.add(-1, 0, 0)).getBlock() == Blocks.LAVA
            )
        ) {
            drawShapeOutline(
                matrixStack,
                vertexConsumer,
                blockState.getOutlineShape(
                    world,
                    blockPos,
                    ShapeContext.of(entity)
                ),
                (double)blockPos.getX() - d,
                (double)blockPos.getY() - e,
                (double)blockPos.getZ() - f,
                1F,
                0.2F,
                0.2F,
                1F
            );
        }
    }
}
