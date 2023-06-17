/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.util

import com.google.common.base.Supplier
import net.minecraft.text.Text

fun Text.supplier(): Supplier<Text> = Supplier { this }