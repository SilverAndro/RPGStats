package io.github.silverandro.rpgstats.stats.internal

import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

class PlayerPreferencesComponent(var playerEntity: PlayerEntity) : PlayerComponent<Component> {
    var isOptedOutOfButtonSpam = false
    override fun readFromNbt(compoundTag: NbtCompound) {
        isOptedOutOfButtonSpam = compoundTag.getBoolean("optedOutSpam")
    }

    override fun writeToNbt(compoundTag: NbtCompound) {
        compoundTag.putBoolean("optedOutSpam", isOptedOutOfButtonSpam)
    }
}