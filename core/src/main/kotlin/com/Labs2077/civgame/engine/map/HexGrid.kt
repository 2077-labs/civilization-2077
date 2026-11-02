package com.Labs2077.civgame.engine.map

/**
 * Reprezentuje jednolitą mapę heksagonów na planie prostokąta o zadanym rozmiarze.
 */
class HexGrid<Tile>(val width: Int, val height: Int, val tiles: Array<Tile>) {

    init {
        require(width >= 0) { "Szerokość mapy musi być nieujemna." }
        require(height >= 0) { "Wysokość mapy musi być nieujemna." }
        require(tiles.size == width * height) { "Ilość kafelków musi być równa iloczynowi szerokości i wysokości." }
    }

    /**
     * Pobiera wartość kafelka na wskazanych współrzędnych.
     */
    operator fun get(at: HexCoordinate): Tile {
        return tiles[at.toIndex()]
    }

    /**
     * Ustawia wartość kafelka na wskazanych współrzędnych.
     */
    operator fun set(at: HexCoordinate, value: Tile) {
        tiles[at.toIndex()] = value
    }

    /**
     * Sprawdza, czy mapa zawiera kafelek na wskazanych współrzędnych.
     */
    operator fun contains(coordinate: HexCoordinate): Boolean = when {
        coordinate.r !in 0..<height -> false
        coordinate.column !in 0..<width -> false
        else -> true
    }

    /**
     * Konwertuje współrzędne kafelka na indeks kolumny.
     */
    private val HexCoordinate.column: Int
        get() = q + r / 2

    /**
     * Konwertuje współrzędne kafelka na indeks w jednowymiarowej tablicy kafelków.
     */
    private fun HexCoordinate.toIndex(): Int {
        if (!this@HexGrid.contains(this)) {
            throw IndexOutOfBoundsException("Współrzędna $this znajduje się poza obszarem mapy.")
        }
        return r * width + column
    }
}
