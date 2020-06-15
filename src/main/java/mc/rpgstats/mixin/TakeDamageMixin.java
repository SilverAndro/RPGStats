package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class TakeDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void dodge(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity spe = (ServerPlayerEntity)(Object)this;
        int level = RPGStats.getComponentLevel(RPGStats.DEFENSE_COMPONENT, ComponentProvider.fromEntity(spe));
        float chance = 0f;
        if (level >= 50) {
            chance = 0.1f;
        } else if (level >= 25) {
            chance = 0.05f;
        }
        if (spe.getRandom().nextDouble() <= chance) {
            cir.cancel();
        }
    }
}
