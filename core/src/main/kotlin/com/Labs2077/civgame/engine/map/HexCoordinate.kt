package com.Labs2077.civgame.engine.map

import kotlin.math.abs

/**
 * Reprezentuje pozycję heksagonu w układzie współrzędnych osiowych.
 *
 * @property q pierwsza współrzędna osiowa.
 * @property r druga współrzędna osiowa.
 * @property s trzecia współrzędna układu Cube, wyliczana na podstawie q i r.
 */
data class HexCoordinate(
    val q: Int,
    val r: Int
) {
    val s: Int
        get() = -q - r
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
    fun distanceTo(other: HexCoordinate): Int {
        val deltaQ = q - other.q
        val deltaR = r - other.r
        val deltaS = s - other.s

        return (abs(deltaQ) + abs(deltaR) + abs(deltaS)) / 2
    }
}
