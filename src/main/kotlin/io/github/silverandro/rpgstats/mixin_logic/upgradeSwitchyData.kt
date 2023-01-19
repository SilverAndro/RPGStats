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
        profileList.getCompound(profileName).put("rpgstats:stats", NbtCompound().apply {
            if (stats != null) {
                put("rpgstats:stats", stats)
            }

            if (internal != null) {
                put("rpgstats:internal", internal)
            }
        })
    }
}