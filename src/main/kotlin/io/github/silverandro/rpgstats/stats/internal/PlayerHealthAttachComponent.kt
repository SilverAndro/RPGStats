package io.github.silverandro.rpgstats.stats.internal

import dev.onyxstudios.cca.api.v3.component.Component
import io.github.silverandro.rpgstats.Events
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

class PlayerHealthAttachComponent(var playerEntity: PlayerEntity) : Component {
    override fun readFromNbt(compoundTag: NbtCompound) {
        if (playerEntity is ServerPlayerEntity) {
            Events.needsStatFix.add(playerEntity as ServerPlayerEntity)
        }
    }

    override fun writeToNbt(compoundTag: NbtCompound) {}
}