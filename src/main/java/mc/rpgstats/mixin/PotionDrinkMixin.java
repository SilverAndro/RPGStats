package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(PotionItem.class)
public class PotionDrinkMixin {
    @Inject(at = @At("HEAD"), method = "finishUsing", cancellable = true)
    private void onFinishedUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
        if (playerEntity instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)playerEntity, stack);
        }

        if (!world.isClient) {
            List<StatusEffectInstance> list = PotionUtil.getPotionEffects(stack);

            for (StatusEffectInstance statusEffectInstance : list) {
                if (statusEffectInstance.getEffectType().isInstant()) {
                    statusEffectInstance.getEffectType().applyInstantEffect(playerEntity, playerEntity, user, statusEffectInstance.getAmplifier(), 1.0D);
                } else {
                    user.addStatusEffect(new StatusEffectInstance(
                            statusEffectInstance.getEffectType(),
                            statusEffectInstance.getDuration() + (RPGStats.getComponentLevel(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(playerEntity)) * 2),
                            statusEffectInstance.getAmplifier(),
                            statusEffectInstance.isAmbient(),
                            statusEffectInstance.shouldShowParticles(),
                            statusEffectInstance.shouldShowIcon()
                    ));
                }
            }
        }

        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat((PotionItem)(Object)this));
            if (!playerEntity.abilities.creativeMode) {
                stack.decrement(1);
            }
        }

        if (playerEntity == null || !playerEntity.abilities.creativeMode) {
            if (stack.isEmpty()) {
                cir.setReturnValue(new ItemStack(Items.GLASS_BOTTLE));
            }

            if (playerEntity != null) {
                playerEntity.inventory.insertStack(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        cir.setReturnValue(stack);
    }

    @Inject(at = @At("HEAD"), method = "getMaxUseTime", cancellable = true)
    private void getUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Entity holder = stack.getHolder();
        if (holder instanceof PlayerEntity) {
            PlayerEntity spe = (PlayerEntity)holder;
            cir.setReturnValue((int) (32 - Math.floor(RPGStats.getComponentLevel(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(spe)) / 3.0f)));
        }
    }
}
