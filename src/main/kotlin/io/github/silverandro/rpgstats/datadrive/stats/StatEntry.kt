/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.datadrive.stats

import kotlinx.serialization.Serializable

@Serializable
data class StatEntry(
    val translationKey: String,
    val shouldRemove: Boolean = false,
    val shouldShowToUser: Boolean = true
)
