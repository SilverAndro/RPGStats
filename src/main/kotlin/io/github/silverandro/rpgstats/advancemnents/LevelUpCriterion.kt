/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.advancemnents

import com.google.gson.JsonObject
import io.github.silverandro.rpgstats.Constants.ANY_ID
import io.github.silverandro.rpgstats.LevelUtils.getHighestLevel
import io.github.silverandro.rpgstats.advancemnents.LevelUpCriterion.LevelCriteria
import io.github.silverandro.rpgstats.stats.Components
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.unmapped.C_ctsfmifk
import net.minecraft.util.Identifier
import java.util.*

class LevelUpCriterion : AbstractCriterion<LevelCriteria>() {
    fun trigger(player: ServerPlayerEntity) {
        this.trigger(player) { levelCriteria: LevelCriteria -> levelCriteria.matches(player) }
    }

    class LevelCriteria(
        optional: Optional<C_ctsfmifk>,
        private val level: Int,
        id: String
    ) : AbstractCriterionConditions(
        optional
    ) {
        private val statId: Identifier = Identifier(id)

        fun matches(player: ServerPlayerEntity): Boolean {
            return if (statId == ANY_ID) getHighestLevel(player) >= level else Components.STATS.get(player)
                .getOrCreateID(statId).level >= level
        }

        override fun toJson(): JsonObject {
            val jsonObject = super.toJson()
            jsonObject.addProperty("level", level)
            jsonObject.addProperty("stat", statId.toString())
            return jsonObject
        }
    }

    override fun method_27854(
        jsonObject: JsonObject,
        optional: Optional<C_ctsfmifk>,
        advancementEntityPredicateDeserializer: AdvancementEntityPredicateDeserializer
    ): LevelCriteria {
        return LevelCriteria(optional, jsonObject["level"].asInt, jsonObject["stat"].asString)
    }
}