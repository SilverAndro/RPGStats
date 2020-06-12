package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
				System.out.println(entity);
				System.out.println(RPGStats.getComponentXP(RPGStats.MELEE_COMPONENT, ComponentProvider.fromEntity(entity)));
			}
		}
	}
}
