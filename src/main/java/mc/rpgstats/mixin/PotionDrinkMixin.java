package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.StatComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionDrinkMixin {
    @Redirect(
        method = "finishUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"
        )
    )
    private boolean onFinishedUsing(LivingEntity livingEntity, StatusEffectInstance effect) {
        if (livingEntity instanceof PlayerEntity) {
            return livingEntity.addStatusEffect(new StatusEffectInstance(
                effect.getEffectType(),
                effect.getDuration() + (RPGStats.getComponentLevel(StatComponents.MAGIC_COMPONENT, (ServerPlayerEntity)livingEntity) * 2),
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.shouldShowParticles(),
                effect.shouldShowIcon()
            ));
        } else {
            return livingEntity.addStatusEffect(effect);
        }
    }
    
    @Inject(at = @At("HEAD"), method = "getMaxUseTime", cancellable = true)
    private void getUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getHolder() != null && stack.getHolder() instanceof PlayerEntity) {
            PlayerEntity holder = (PlayerEntity)stack.getHolder();
            cir.setReturnValue((int)(32 - Math.floor(RPGStats.getComponentLevel(StatComponents.MAGIC_COMPONENT, (ServerPlayerEntity)holder) / 3.0f)));
        }
    }
}
