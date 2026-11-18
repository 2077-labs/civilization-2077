package com.labs2077.civilization.core.models

import kotlin.math.abs

data class Hexagon(val q: Int, val r: Int) {
    val s: Int get() = -q - r

    /** Zwraca współrzędne sąsiadujących heksagonów. */
    fun neighbors() = setOf(
        Hexagon(q + 1, r),
        Hexagon(q + 1, r - 1),
        Hexagon(q, r - 1),
        Hexagon(q - 1, r),
        Hexagon(q - 1, r + 1),
        Hexagon(q, r + 1)
    )

    /**
     * Oblicza minimalną odległość geometryczną między dwoma heksagonami.
     *
     * Odległość oznacza minimalną liczbę przejść między sąsiednimi
     * heksagonami na nieograniczonej siatce, bez uwzględniania przeszkód
     * i kosztów ruchu.
     *
     * Wykorzystuje współrzędne Cube (q, r, s).
     *
     * @param other współrzędne heksagonu, do którego mierzymy odległość.
     * @return minimalna liczba przejść między heksagonami.
     */
    fun distanceTo(other: Hexagon): Int {
        val distanceQ = abs(q - other.q)
        val distanceR = abs(r - other.r)
        val distanceS = abs(s - other.s)

        return (distanceQ + distanceR + distanceS) / 2
    }
}
