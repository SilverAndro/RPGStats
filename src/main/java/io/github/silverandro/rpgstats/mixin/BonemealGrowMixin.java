package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.mixin_logic.OnSneakLogic;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class BonemealGrowMixin {
    @Inject(method = "setSneaking", at = @At("TAIL"))
    public void rpgstats$onShift(boolean sneaking, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Entity) (Object) this instanceof ServerPlayerEntity) {
            if (!Components.PREFERENCES.get(this).isOptedOutOfButtonSpam()) {
                OnSneakLogic.doLogic(sneaking, (ServerPlayerEntity) (Object) this);
            }
        }
    }
}