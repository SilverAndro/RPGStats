package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public abstract class KillMixin {
	@Inject(at = @At("HEAD"), method = "onDeath")
	private void onKilledXPTracker(DamageSource source, CallbackInfo info) {
		LivingEntity le = (LivingEntity)(Object)this;
		if (!le.world.isClient && !le.removed) {
			Entity entity = source.getAttacker();
			if (entity instanceof ServerPlayerEntity) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity)entity;
				if (source.isProjectile()) {
					if (le instanceof WitherEntity || le instanceof EnderDragonEntity) {
						RPGStats.addXpAndLevelUp(CustomComponents.RANGED_COMPONENT, serverPlayer, 130);
					} else {
						RPGStats.addXpAndLevelUp(CustomComponents.RANGED_COMPONENT, serverPlayer, 1);
					}
				} else if (source.getMagic()) {
					RPGStats.addXpAndLevelUp(CustomComponents.MAGIC_COMPONENT, serverPlayer, 1);
				} else if (!source.isExplosive() && !source.isFire()) {
					if (le instanceof PassiveEntity) {
						RPGStats.addXpAndLevelUp(CustomComponents.FARMING_COMPONENT, serverPlayer, 1);
					} else {
						int level = RPGStats.getComponentLevel(CustomComponents.MELEE_COMPONENT, serverPlayer);
						
						int duration = level >= 25 ? level >= 50 ? 200 : 100 : 0;
						if (duration > 0) {
							((ServerPlayerEntity)entity).addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration));
						}
						
						if (le instanceof WitherEntity || le instanceof EnderDragonEntity) {
							RPGStats.addXpAndLevelUp(CustomComponents.MELEE_COMPONENT, serverPlayer, 130);
						} else {
							RPGStats.addXpAndLevelUp(CustomComponents.MELEE_COMPONENT, serverPlayer, 1);
						}
					}
				}
			}
		}
	}
}
