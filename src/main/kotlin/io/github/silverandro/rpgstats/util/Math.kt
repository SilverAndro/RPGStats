/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.util

import kotlin.math.floor

val Double.isInteger: Boolean
    inline get() = this == floor(this) && !isInfinite()

val Double.cleanDisplay: String
    inline get() = if (isInteger) toInt().toString() else toString()