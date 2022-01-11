package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class TakeDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void rpgstats$dodge(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity spe = (ServerPlayerEntity)(Object)this;
        int level = RPGStats.getComponentLevel(CustomComponents.DEFENSE, spe);
        float chance = 0f;
        if (level >= 50 && RPGStats.getConfig().toggles.defense.enableLv50Buff) {
            chance = 0.1f;
        } else if (level >= 25 && RPGStats.getConfig().toggles.defense.enableLv25Buff) {
            chance = 0.05f;
        }
        if (spe.getRandom().nextDouble() <= chance) {
            cir.cancel();
        }
    }
    
    @Inject(method = "onDeath", at = @At("HEAD"))
    public void rpgstats$onPlayerDeathRefreshStats(DamageSource source, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object)this instanceof ServerPlayerEntity) {
            ServerPlayerEntity le = (ServerPlayerEntity)(Object)this;
            if (!le.world.isClient) {
                RPGStats.needsStatFix.add(le);
            }
        }
    }
}
