package com.labs2077.civilization.core.models

/**
 * Reprezentuje jednolitą mapę heksagonów na planie prostokąta o zadanym rozmiarze.
 */
class HexagonGrid<Cell>(val width: Int, val height: Int, private val tiles: Array<Cell>) :
    Iterable<Cell> {

    init {
        require(width >= 0) { "Szerokość mapy musi być nieujemna." }
        require(height >= 0) { "Wysokość mapy musi być nieujemna." }
        require(tiles.size == width * height) { "Ilość kafelków musi być równa iloczynowi szerokości i wysokości." }
    }

    /**
     * Pobiera wartość komórki na wskazanych współrzędnych.
     */
    operator fun get(at: Hexagon): Cell {
        return tiles[at.toIndex()]
    }

    /**
     * Ustawia wartość komórki na wskazanych współrzędnych.
     */
    operator fun set(at: Hexagon, value: Cell) {
        tiles[at.toIndex()] = value
    }

    /**
     * Sprawdza, czy mapa zawiera komórkę na wskazanych współrzędnych.
     */
    operator fun contains(coordinate: Hexagon): Boolean = when {
        coordinate.r !in 0..<height -> false
        coordinate.column !in 0..<width -> false
        else -> true
    }

    /**
     * Konwertuje współrzędne komórki na indeks kolumny.
     */
    private val Hexagon.column: Int
        get() = q + r / 2

    /**
     * Konwertuje współrzędne komórki na indeks w jednowymiarowej tablicy komórek.
     */
    private fun Hexagon.toIndex(): Int {
        if (!this@HexagonGrid.contains(this)) {
            throw IndexOutOfBoundsException("Współrzędna $this znajduje się poza obszarem mapy.")
        }
        return r * width + column
    }

    /**
     * Zwraca iterator przechodzący przez wszystkie komórki.
     */
    override fun iterator(): Iterator<Cell> {
        return tiles.iterator()
    }

    /**
     * Zwraca iterator, który do wartości komórki dołącza jej współrzędne.
     */
    fun withCoordinate(): CoordinateIterator {
        return CoordinateIterator()
    }

    /**
     * Iterator, który do wartości komórki dołącza jej współrzędne.
     */
    inner class CoordinateIterator : Iterator<Pair<Hexagon, Cell>> {
        private var index = -1

        override fun next(): Pair<Hexagon, Cell> {
            if (!hasNext()) {
                throw NoSuchElementException("Nie ma już więcej elementów mapy.")
            }

            index += 1

            val coordinate = index.toHexagon(width, height)
            val tile: Cell = tiles[index]

            return coordinate to tile
        }

        override fun hasNext(): Boolean {
            return (index + 1) in tiles.indices
        }
    }

    companion object {
        /**
         * Konwertuje indeks w tablicy jednowymiarowej na współrzędne komórki.
         */
        private fun Int.toHexagon(width: Int, height: Int): Hexagon {
            if (this !in 0 until width * height) {
                throw IndexOutOfBoundsException("Indeks $this znajduje się poza obszarem mapy.")
            }

            val row = this / width
            val column = this % width

            val q = column - row / 2

            return Hexagon(q, row)
        }
    }
}
