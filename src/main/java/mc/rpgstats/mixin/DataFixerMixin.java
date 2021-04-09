package mc.rpgstats.mixin;

import mc.rpgstats.component.StatsEntry;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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
                    newTag.put(key, components.getCompound(key));
                }
            }
        }
        
        for (String key : newTag.getKeys()) {
            StatsEntry entry = CustomComponents.STATS.get(this).getOrCreateID(Identifier.tryParse(key));
            entry.level = newTag.getCompound(key).getInt("level");
            entry.xp = newTag.getCompound(key).getInt("xp");
        }
    }
}
