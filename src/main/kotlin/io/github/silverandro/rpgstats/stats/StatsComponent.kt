package io.github.silverandro.rpgstats.stats

import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import io.github.silverandro.rpgstats.Constants.SYNC_STATS_PACKET_ID
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import java.util.*
import java.util.function.Consumer

class StatsComponent(private val playerEntity: PlayerEntity) : Component, AutoSyncedComponent {
    var entries = HashMap<Identifier, StatEntry>()
    override fun shouldSyncWith(player: ServerPlayerEntity): Boolean {
        return ServerPlayNetworking.canSend(player, SYNC_STATS_PACKET_ID)
    }

    override fun readFromNbt(compoundTag: NbtCompound) {
        entries.clear()
        compoundTag.keys.forEach(Consumer { s: String ->
            val identifier = Identifier.tryParse(s)
            if (identifier != null) {
                val data = compoundTag.getCompound(identifier.toString())!!
                entries[identifier] = StatEntry(identifier, data.getInt("level"), data.getInt("xp"))
            } else {
                System.err.println("Failed to parse stat identifier: $s")
            }
        })
    }

    override fun writeToNbt(compoundTag: NbtCompound) {
        for (entry in entries.values) {
            entry.addToCompound(compoundTag)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other!!.javaClass) return false
        val that = other as StatsComponent?
        return playerEntity == that!!.playerEntity && entries == that.entries
    }

    override fun hashCode(): Int {
        return Objects.hash(playerEntity, entries)
    }

    fun getOrCreateID(id: Identifier): StatEntry {
        return entries.computeIfAbsent(id) { identifier: Identifier? -> StatEntry(id, 0, 0) }
    }
}