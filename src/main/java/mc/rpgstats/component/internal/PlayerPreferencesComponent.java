package mc.rpgstats.component.internal;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class PlayerPreferencesComponent implements PlayerComponent<Component> {
    public PlayerEntity playerEntity;

    public boolean isOptedOutOfButtonSpam = false;

    public PlayerPreferencesComponent(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }

    @Override
    public void readFromNbt(NbtCompound compoundTag) {
        isOptedOutOfButtonSpam = compoundTag.getBoolean("optedOutSpam");
    }

    @Override
    public void writeToNbt(NbtCompound compoundTag) {
        compoundTag.putBoolean("optedOutSpam", isOptedOutOfButtonSpam);
    }
}