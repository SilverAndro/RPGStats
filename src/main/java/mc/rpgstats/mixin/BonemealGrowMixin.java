package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.StatComponents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.Entity;
import net.minecraft.item.HoeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Entity.class)
public class BonemealGrowMixin {
    @Final
    @Shadow
    protected Random random;

    @Inject(method = "setSneaking", at = @At("TAIL"))
    public void onShift(boolean sneaking, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Entity) (Object) this instanceof ServerPlayerEntity) {
            if (sneaking && ((ServerPlayerEntity)(Object)this).getMainHandStack().getItem() instanceof HoeItem && new Random().nextBoolean()) {
                Entity entity = (Entity)(Object)this;
                int level = RPGStats.getComponentLevel(StatComponents.FARMING_COMPONENT, (ServerPlayerEntity)entity);
                World world = entity.world;
                int amount = level >= 25 ? level >= 50 ? 5 : 3 : 0;
                BlockPos blockPos = entity.getBlockPos();

                if (amount > 0) {
                    for (int y = -1; y <= 1; y++) {
                        for (int x = -amount; x <= amount; x++) {
                            for (int z = -amount; z <= amount; z++) {
                                BlockPos nextPos = blockPos.add(x, y, z);
                                BlockState bs = world.getBlockState(nextPos);
                                if (bs.getBlock() instanceof Fertilizable) {
                                    if (random.nextDouble() > 0.9) {
                                        ((Fertilizable) bs.getBlock()).grow((ServerWorld) world, world.random, nextPos, bs);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
