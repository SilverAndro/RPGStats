package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class KillMixin {
	@Inject(at = @At("HEAD"), method = "onDeath")
	private void onKilledXPTracker(DamageSource source, CallbackInfo info) {
		LivingEntity le = (LivingEntity)(Object)this;
		if (!le.world.isClient && !le.removed) {
			Entity entity = source.getAttacker();
			if (entity instanceof ServerPlayerEntity) {
				ComponentProvider provider = ComponentProvider.fromEntity(entity);
				if (source.isProjectile()) {
					RPGStats.addXpAndLevelUpIfNeeded(RPGStats.RANGED_COMPONENT, provider, 1);
				} else if (source.getMagic()) {
					RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MAGIC_COMPONENT, provider, 1);
				} else if (!source.isExplosive() && !source.isFire()) {
					// Not always false
					//noinspection ConstantConditions
					if (le instanceof PassiveEntity) {
						RPGStats.addXpAndLevelUpIfNeeded(RPGStats.FARMING_COMPONENT, provider, 1);
					} else {
						RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MELEE_COMPONENT, provider, 1);
					}
				}
			}
		}
	}
}
