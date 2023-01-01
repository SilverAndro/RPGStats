package io.github.silverandro.rpgstats.datadrive.stats

import kotlinx.serialization.Serializable

@Serializable
data class StatEntry(
    val translationKey: String,
    val shouldRemove: Boolean = false,
    val shouldShowToUser: Boolean = true
)
