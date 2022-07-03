package io.github.silverandro.rpgstats.util

import kotlin.math.floor

val Double.isInteger: Boolean
    inline get() = this == floor(this) && !isInfinite()

val Double.cleanDisplay: String
    inline get() = if (isInteger) toInt().toString() else toString()