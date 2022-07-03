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