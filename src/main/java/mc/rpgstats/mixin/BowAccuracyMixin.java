package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.entity.Entity;
import net.minecraft.item.BowItem;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BowItem.class)
class BowAccuracyMixin {
    @ModifyArg(
        method = "onStoppedUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V"
        ),
        index = 5
    )
    public float rpgstats$changeAccuracy(Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        if (shooter instanceof ServerPlayerEntity playerEntity) {
            return 1.0f - RPGStats.getComponentLevel(CustomComponents.RANGED, playerEntity) / 50f;
        }
        return divergence;
    }
}
