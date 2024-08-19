/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.datadrive.xp

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.util.Identifier

object XpData {
    @Serializable
    @JvmRecord
    data class XpEntry(val id: @Contextual Identifier, val amount: Int, val chance: Double = 1.0) {
        companion object {
            val RAW_CODEC = RecordCodecBuilder.create { i ->
                i.group(
                    Identifier.CODEC.fieldOf("id").forGetter(XpEntry::id),
                    Codec.INT.fieldOf("amount").forGetter(XpEntry::amount),
                    Codec.DOUBLE.fieldOf("chance").forGetter(XpEntry::chance)
                ).apply(i, ::XpEntry)
            }

            val CODEC = Codec.either(RAW_CODEC, Codec.list(RAW_CODEC))
        }
    }

    val BLOCK_XP = mutableMapOf<Block, MutableList<XpEntry>>()
    val ENTITY_XP_OVERRIDE = mutableMapOf<EntityType<*>, MutableList<XpEntry>>()
}
