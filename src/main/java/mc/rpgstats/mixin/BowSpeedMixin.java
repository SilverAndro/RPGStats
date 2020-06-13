package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
class BowAccuracyMixin {
    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setProperties(Lnet/minecraft/entity/Entity;FFFFF)V",
                shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void changeAccuracy(
            ItemStack stack,
            World world,
            LivingEntity user,
            int remainingUseTicks,
            CallbackInfo ci,
            PlayerEntity playerEntity,
            boolean bl,
            ItemStack itemStack,
            int i,
            float f,
            boolean bl2,
            ArrowItem arrowItem,
            PersistentProjectileEntity persistentProjectileEntity
    ) {
        float newDistort = 1.0f - RPGStats.getComponentLevel(RPGStats.RANGED_COMPONENT, ComponentProvider.fromEntity(playerEntity)) / 50f;
        persistentProjectileEntity.setProperties(playerEntity, playerEntity.pitch, playerEntity.yaw, 0.0F, f * 3.0F, newDistort);
    }
}
