package io.github.silverandro.rpgstats.mixin_logic

import net.minecraft.nbt.NbtCompound

// input should be "switchy:presets"/"list"
fun upgradeSwitchyData(profileList: NbtCompound) {
    println("Upgrading switchy data for rpgstats...")

    data class UpgradeEntry(var stats: NbtCompound?, var internal: NbtCompound?) { constructor() : this(null, null) }
    val toUpgrade = mutableMapOf<String, UpgradeEntry>()

    // Extract
    println("Extracting old data...")
    profileList.keys.forEach { profileName ->
        println("Profile: $profileName")
        val profileNbt = profileList.getCompound(profileName)
        profileNbt.keys.forEach { moduleName ->
            println("Module: $moduleName")
            when (moduleName) {
                "rpgstats:switchy_compat" -> toUpgrade.computeIfAbsent(profileName) { UpgradeEntry() }.stats = profileNbt.getCompound(moduleName)
                "rpgstats:switchy_compat_internal" -> toUpgrade.computeIfAbsent(profileName) { UpgradeEntry() }.internal = profileNbt.getCompound(moduleName)
                else -> {}
            }
        }
    }

    println(toUpgrade)
    println("Restoring data...")
    // Restore
    toUpgrade.forEach {  (profileName, entry) ->
        val (stats, internal) = entry
        println("Profile: $profileName ${stats != null} ${internal != null}")
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