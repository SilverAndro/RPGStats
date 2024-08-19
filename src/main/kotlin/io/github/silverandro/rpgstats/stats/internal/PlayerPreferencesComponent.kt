/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.stats.internal

import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import org.ladysnake.cca.api.v3.component.Component

class PlayerPreferencesComponent : Component {
    var isOptedOutOfButtonSpam = false
    var xpBarLocation = XpBarLocation.HOTBAR
    var xpBarShow = XpBarShow.ALWAYS

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        isOptedOutOfButtonSpam = tag.getBoolean("optedOutSpam")
        xpBarLocation = XpBarLocation.valueOf(tag.getString("xpBarLocation").takeUnless { it.isNullOrEmpty() } ?: "HOTBAR")
        xpBarShow = XpBarShow.valueOf(tag.getString("xpBarShow").takeUnless { it.isNullOrEmpty() } ?: "SMART")
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.putBoolean("optedOutSpam", isOptedOutOfButtonSpam)
        tag.putString("xpBarLocation", xpBarLocation.name)
        tag.putString("xpBarShow", xpBarShow.name)
    }
}