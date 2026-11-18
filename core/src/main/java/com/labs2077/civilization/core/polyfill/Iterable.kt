package com.labs2077.civilization.core.polyfill

/**
 * Zwraca `true`, jeżeli we wskazanej kolekcji nie występują duplikaty.
 */
fun <T, V> Iterable<T>.allUniqueBy(selector: (T) -> V): Boolean {
    val originals = hashSetOf<V>()
    return all { originals.add(selector(it)) }
}
