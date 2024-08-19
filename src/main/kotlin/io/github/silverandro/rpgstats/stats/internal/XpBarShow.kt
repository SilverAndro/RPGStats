/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.stats.internal

import com.mojang.serialization.Codec
import net.minecraft.util.StringIdentifiable

enum class XpBarShow : StringIdentifiable {
    NEVER,
    SMART,
    ALWAYS;

    override fun asString() = this.name

    companion object {
        val CODEC: Codec<XpBarShow> = StringIdentifiable.createCodec { XpBarShow.entries.toTypedArray() }
    }
}