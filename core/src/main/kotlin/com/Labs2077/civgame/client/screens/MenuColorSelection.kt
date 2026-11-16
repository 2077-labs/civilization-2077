package com.Labs2077.civgame.client.screens

/**
 * Przechowuje wybór kolorów graczy w menu.
 *
 * Klasa nie zależy od LibGDX, dzięki czemu jej logikę
 * można testować zwykłymi testami jednostkowymi.
 */
class MenuColorSelection(
    palette: List<Int> = DEFAULT_PALETTE,
    initialColorIndices: List<Int> = listOf(0, 3)
) {

    // Dostępna paleta kolorów.
    // Kopiujemy listę, żeby zewnętrzna zmiana nie zmieniła stanu klasy.
    val palette: List<Int> = palette.toList()

    // Indeks wybranego koloru dla każdego gracza.
    private val selectedColorIndices = initialColorIndices.toMutableList()

    init {
        require(this.palette.isNotEmpty()) {
            "Paleta kolorów nie może być pusta."
        }

        require(selectedColorIndices.isNotEmpty()) {
            "Musi istnieć przynajmniej jeden gracz."
        }

        require(selectedColorIndices.all { it in this.palette.indices }) {
            "Początkowy wybór zawiera nieprawidłowy indeks koloru."
        }
    }

    // Liczba graczy obsługiwanych przez wybór kolorów.
    val playerCount: Int
        get() = selectedColorIndices.size

    // Informacja, czy wszyscy gracze mają różne kolory.
    val canStart: Boolean
        get() = selectedColorIndices.distinct().size == selectedColorIndices.size

    /**
     * Ustawia kolor wybranego gracza.
     *
     * playerIndex - indeks gracza.
     * colorIndex - indeks koloru w palecie.
     */
    fun selectColor(playerIndex: Int, colorIndex: Int) {
        require(playerIndex in selectedColorIndices.indices) {
            "Nieprawidłowy indeks gracza: $playerIndex"
        }

        require(colorIndex in palette.indices) {
            "Nieprawidłowy indeks koloru: $colorIndex"
        }

        selectedColorIndices[playerIndex] = colorIndex
    }

    /**
     * Zwraca indeks wybranego koloru gracza.
     */
    fun selectedColorIndex(playerIndex: Int): Int {
        require(playerIndex in selectedColorIndices.indices) {
            "Nieprawidłowy indeks gracza: $playerIndex"
        }

        return selectedColorIndices[playerIndex]
    }

    /**
     * Zwraca wartość koloru wybranego przez gracza.
     */
    fun selectedColor(playerIndex: Int): Int {
        return palette[selectedColorIndex(playerIndex)]
    }

    companion object {
        val DEFAULT_PALETTE = listOf(
            0x00E5FF,
            0xFFB000,
            0x50FF78,
            0xFF3B3B,
            0xF2F2F2,
            0x9B5CFF,
            0xFF7A00,
            0x14141C
        )
    }
}