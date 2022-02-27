package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

// Have to lower priority due to mixin bug with mixin conflict handling
@Mixin(value = PotionItem.class, priority = 900)
public class PotionDrinkMixin {
    // What is this bruh. None of the capturing is documented, and it's a core feature of the annotation
    // Also it literally says Redirect is better :rolling_eyes:
    @ModifyArgs(
        method = "finishUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"
        )
    )
    private void rpgstats$OnFinishDrinkingPotion(Args args, ItemStack stack, World world, LivingEntity entity) {
        // Yay! no type safety
        StatusEffectInstance effect = args.get(0);
        
        if (entity instanceof ServerPlayerEntity playerEntity) {
            RPGStats.addXpAndLevelUp(CustomComponents.MAGIC, playerEntity, 10);
            
            int newDuration;
            if (RPGStats.getComponentLevel(CustomComponents.MAGIC, playerEntity) > 0) {
                newDuration = effect.getDuration() + (effect.getDuration() / ((RPGStats.getConfig().scaling.maxLevel * 5) / RPGStats.getComponentLevel(CustomComponents.MAGIC, playerEntity)));
            } else {
                newDuration = effect.getDuration();
            }
            
            // Why is `permanent` mutable but not anything else
            StatusEffectInstance newInstance = new StatusEffectInstance(
                effect.getEffectType(),
                newDuration,
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.shouldShowParticles(),
                effect.shouldShowIcon()
            );
    
            args.set(0, newInstance);
        }
    }

    @Inject(
            method = "finishUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/effect/StatusEffect;applyInstantEffect(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;ID)V"
            )
    )
    private void rpgstats$OnFinishDrinkingHealthPotion(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof ServerPlayerEntity playerEntity) {
            RPGStats.addXpAndLevelUp(CustomComponents.MAGIC, playerEntity, 10);

        }
    }
    
    @Inject(at = @At("HEAD"), method = "getMaxUseTime", cancellable = true)
    private void rpgstats$getPotionUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getHolder() != null && stack.getHolder() instanceof ServerPlayerEntity holder) {
            cir.setReturnValue((int)(32 - Math.floor(RPGStats.getComponentLevel(CustomComponents.MAGIC, holder) / 3.0f)));
        }
    }
}