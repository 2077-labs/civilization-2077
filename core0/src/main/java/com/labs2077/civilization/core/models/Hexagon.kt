package com.labs2077.civilization.core.models

data class Hexagon(val q: Int, val r: Int) {
    val s: Int get() = -q - r

    /**
     * Zwraca sześć współrzędnych sąsiednich heksagonów.
     */
    fun neighbors() = setOf(
        Hexagon(q + 1, r),
        Hexagon(q + 1, r - 1),
        Hexagon(q, r - 1),
        Hexagon(q - 1, r),
        Hexagon(q - 1, r + 1),
        Hexagon(q, r + 1)
    )
}
