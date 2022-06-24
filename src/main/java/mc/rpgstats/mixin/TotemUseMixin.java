package mc.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public class TotemUseMixin {
    @Inject(
        method = "tryUseTotem(Lnet/minecraft/entity/damage/DamageSource;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;incrementStat(Lnet/minecraft/stat/Stat;)V",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void rpgstats$onUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantConditions
        if ((Object)this instanceof ServerPlayerEntity serverPlayerEntity) {
            LevelUtils.INSTANCE.addXpAndLevelUp(CustomComponents.DEFENSE, serverPlayerEntity, 100);
        }
    }
}