/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.util

inline fun <reified K, reified V> MutableMap<K, V>.filterInPlace(predicate: (K, V) -> Boolean) {
    val toRemove = mutableSetOf<K>()
    toRemove.addAll(this.filter { predicate(it.key, it.value) }.keys)
    toRemove.forEach {
        this.remove(it)
    }
}

inline fun <reified T> MutableList<T>.filterInPlace(predicate: (T) -> Boolean) {
    val toRemove = mutableSetOf<T>()
    toRemove.addAll(this.filter { predicate(it) })
    toRemove.forEach {
        this.remove(it)
    }
}