package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public class KillMixin {
	@Inject(at = @At("HEAD"), method = "onKilledBy")
	private void onKilledXPTracker(LivingEntity adversary, CallbackInfo info) {
		if (!adversary.world.isClient && adversary instanceof ServerPlayerEntity) {
			RPGStats.getComponentXP(RPGStats.MELEE_COMPONENT, ComponentProvider.fromEntity(adversary));
		}
	}
}
