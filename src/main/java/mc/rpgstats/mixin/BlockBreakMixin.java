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
import net.minecraft.state.property.IntProperty;

@Mixin(Block.class)
public class BlockBreakMixin {
    @Inject(at = @At("HEAD"), method = "onBreak")
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (!world.isClient) {
            Block block = state.getBlock();
            if (block instanceof PlantBlock || block instanceof PumpkinBlock || block instanceof MelonBlock) {
                RPGStats.addXpAndLevelUpIfNeeded(RPGStats.FARMING_COMPONENT, ComponentProvider.fromEntity(player), 1);
            }
        }
    }
}
