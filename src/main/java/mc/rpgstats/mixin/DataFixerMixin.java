package mc.rpgstats.mixin;

import mc.rpgstats.main.CustomComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class DataFixerMixin {
    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    public void upgradeStatDataTov2(CompoundTag tag, CallbackInfo ci) {
        CompoundTag components = tag.getCompound("cardinal_components");
        
        CompoundTag newTag = components.getCompound("rpgstats:stats");
        
        for (String key : components.getKeys()) {
            if (key.startsWith("rpgstats:")) {
                if (
                    !key.equals("rpgstats:stats") &&
                        !key.equals("rpgstats:max_health") &&
                        !key.equals("rpgstats:internal")
                ) {
                    System.out.println(key);
                    newTag.put(key, components.getCompound(key));
                }
            }
        }
    }
}
