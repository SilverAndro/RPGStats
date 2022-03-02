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
	private void rpgstats$onKilledXPTracker(DamageSource source, CallbackInfo info) {
		LivingEntity le = (LivingEntity)(Object)this;
		if (!le.world.isClient && !le.isRemoved()) {
			Entity entity = source.getAttacker();
			if (entity instanceof ServerPlayerEntity serverPlayer) {
				if (source.isProjectile()) {
					if (le instanceof WitherEntity || le instanceof EnderDragonEntity) {
						RPGStats.addXpAndLevelUp(CustomComponents.RANGED, serverPlayer, 130);
					} else {
						RPGStats.addXpAndLevelUp(CustomComponents.RANGED, serverPlayer, 1);
					}
				} else if (source.isMagic()) {
					RPGStats.addXpAndLevelUp(CustomComponents.MAGIC, serverPlayer, 1);
				} else if (!source.isExplosive() && !source.isFire()) {
					if (le instanceof PassiveEntity) {
						RPGStats.addXpAndLevelUp(CustomComponents.FARMING, serverPlayer, 1);
					} else {
						int level = RPGStats.getComponentLevel(CustomComponents.MELEE, serverPlayer);
						
						int duration = 0;
						if (level >= 50 && RPGStats.getConfig().toggles.melee.enableLv50Buff) {
							duration = 200;
						} else if (level >= 25 && RPGStats.getConfig().toggles.melee.enableLv25Buff) {
							duration = 100;
						}
						
						if (duration > 0) {
							((ServerPlayerEntity)entity).addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration));
						}
						
						if (le instanceof WitherEntity || le instanceof EnderDragonEntity) {
							RPGStats.addXpAndLevelUp(CustomComponents.MELEE, serverPlayer, 130);
						} else {
							RPGStats.addXpAndLevelUp(CustomComponents.MELEE, serverPlayer, 1);
						}
					}
				}
			}
		}
	}
}
