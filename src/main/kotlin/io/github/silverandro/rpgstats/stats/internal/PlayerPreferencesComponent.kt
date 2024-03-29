/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.stats.internal

import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

class PlayerPreferencesComponent(var playerEntity: PlayerEntity) : PlayerComponent<Component> {
    var isOptedOutOfButtonSpam = false
    var xpBarLocation = XpBarLocation.HOTBAR
    var xpBarShow = XpBarShow.ALWAYS

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