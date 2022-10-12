package io.github.silverandro.rpgstats.mixin;

import io.github.silverandro.rpgstats.stats.Components;
import io.github.silverandro.rpgstats.stats.StatEntry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public class DataFixerMixin {
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void rpgstats$upgradeStatDataTov2(NbtCompound tag, CallbackInfo ci) {
        NbtCompound components = tag.getCompound("cardinal_components");

        NbtCompound newTag = components.getCompound("rpgstats:stats");

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
            StatEntry entry = Components.STATS.get(this).getOrCreateID(Objects.requireNonNull(Identifier.tryParse(key)));
            entry.setLevel(newTag.getCompound(key).getInt("level"));
            entry.setXp(newTag.getCompound(key).getInt("xp"));
        }
    }
}