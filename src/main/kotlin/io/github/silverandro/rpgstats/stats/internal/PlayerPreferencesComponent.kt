package io.github.silverandro.rpgstats.stats.internal

import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

class PlayerPreferencesComponent(var playerEntity: PlayerEntity) : PlayerComponent<Component> {
    var isOptedOutOfButtonSpam = false
    var xpBarLocation = XpBarLocation.HOTBAR
    var xpBarShow = XpBarShow.SMART

    override fun readFromNbt(compoundTag: NbtCompound) {
        isOptedOutOfButtonSpam = compoundTag.getBoolean("optedOutSpam")
        xpBarLocation = XpBarLocation.valueOf(compoundTag.getString("xpBarLocation").takeUnless { it.isNullOrEmpty() } ?: "HOTBAR")
        xpBarShow = XpBarShow.valueOf(compoundTag.getString("xpBarShow").takeUnless { it.isNullOrEmpty() } ?: "SMART")
    }

    override fun writeToNbt(compoundTag: NbtCompound) {
        compoundTag.putBoolean("optedOutSpam", isOptedOutOfButtonSpam)
        compoundTag.putString("xpBarLocation", xpBarLocation.name)
        compoundTag.putString("xpBarShow", xpBarShow.name)
    }
}