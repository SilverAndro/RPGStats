/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin_logic

import net.minecraft.nbt.NbtCompound

// input should be "switchy:presets"/"list"
fun upgradeSwitchyData(profileList: NbtCompound) {
    data class UpgradeEntry(var stats: NbtCompound?, var internal: NbtCompound?) { constructor() : this(null, null) }
    val toUpgrade = mutableMapOf<String, UpgradeEntry>()

    // Extract
    profileList.keys.forEach { profileName ->
        val profileNbt = profileList.getCompound(profileName)
        profileNbt.keys.forEach { moduleName ->
            when (moduleName) {
                "rpgstats:switchy_compat" -> toUpgrade.computeIfAbsent(profileName) { UpgradeEntry() }.stats = profileNbt.getCompound(moduleName)
                "rpgstats:switchy_compat_internal" -> toUpgrade.computeIfAbsent(profileName) { UpgradeEntry() }.internal = profileNbt.getCompound(moduleName)
                else -> {}
            }
        }
    }

    // Restore
    toUpgrade.forEach {  (profileName, entry) ->
        val (stats, internal) = entry
        if (stats != null && internal != null)
        profileList.getCompound(profileName).apply {
            put("rpgstats:stats", NbtCompound().apply {
                put("rpgstats:stats", stats)
                put("rpgstats:internal", internal)
            })
            remove("rpgstats:internal")
        }
    }
}