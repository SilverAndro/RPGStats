package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LivingEntity.class)
public abstract class KillMixin {
	@Shadow public abstract Random getRandom();

	// It gets confused cause im casting myself to LivingEntity
	@SuppressWarnings("ConstantConditions")

	@Inject(at = @At("HEAD"), method = "onDeath")
	private void onKilledXPTracker(DamageSource source, CallbackInfo info) {
		LivingEntity le = (LivingEntity)(Object)this;
		if (!le.world.isClient && !le.removed) {
			Entity entity = source.getAttacker();
			if (entity instanceof ServerPlayerEntity) {
				ComponentProvider provider = ComponentProvider.fromEntity(entity);
				if (source.isProjectile()) {
					if (le instanceof WitherEntity || le instanceof EnderDragonEntity) {
						RPGStats.addXpAndLevelUpIfNeeded(RPGStats.RANGED_COMPONENT, provider, 50);
					} else {
						RPGStats.addXpAndLevelUpIfNeeded(RPGStats.RANGED_COMPONENT, provider, 1);
					}
				} else if (source.getMagic()) {
					RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MAGIC_COMPONENT, provider, 1);
				} else if (!source.isExplosive() && !source.isFire()) {
					if (le instanceof PassiveEntity) {
						RPGStats.addXpAndLevelUpIfNeeded(RPGStats.FARMING_COMPONENT, provider, 1);
					} else {
						if (le instanceof WitherEntity || le instanceof EnderDragonEntity) {
							RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MELEE_COMPONENT, provider, 50);
						} else if (le instanceof EndermanEntity) {
							if (getRandom().nextDouble() > 0.60) {
								RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MELEE_COMPONENT, provider, 1);
							}
						} else {
							RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MELEE_COMPONENT, provider, 1);
						}
					}
				}
			}
		}
	}
}
