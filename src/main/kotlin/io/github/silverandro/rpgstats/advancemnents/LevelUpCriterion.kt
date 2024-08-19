/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.advancemnents

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.silverandro.rpgstats.Constants.ANY_ID
import io.github.silverandro.rpgstats.LevelUtils.getHighestLevel
import io.github.silverandro.rpgstats.advancemnents.LevelUpCriterion.LevelCriteria
import io.github.silverandro.rpgstats.stats.Components
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.util.*

class LevelUpCriterion : AbstractCriterion<LevelCriteria>() {
    fun trigger(player: ServerPlayerEntity) {
        this.trigger(player) { levelCriteria: LevelCriteria -> levelCriteria.matches(player) }
    }

    @JvmRecord
    data class LevelCriteria(val statID: Identifier, val level: Int) : Conditions {
        fun matches(player: ServerPlayerEntity): Boolean {
            return if (statID == ANY_ID) getHighestLevel(player) >= level else Components.STATS.get(player)
                .getOrCreateID(statID).level >= level
        }

        override fun player(): Optional<LootContextPredicate> {
            return Optional.empty()
        }
    }
    override fun getConditionsCodec(): Codec<LevelCriteria> {
        return CODEC
    }

    companion object {
        val CODEC: Codec<LevelCriteria> = RecordCodecBuilder.create { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("statID").forGetter(LevelCriteria::statID),
                Codec.INT.fieldOf("level").forGetter(LevelCriteria::level)
            ).apply(
                instance,
                ::LevelCriteria
            )
        }
    }
}