package com.Labs2077.civgame.client.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuColorSelectionTest {

    @Test
    fun defaultSelectionHasDifferentColors() {
        val selection = MenuColorSelection()

        assertTrue(selection.canStart)
        assertEquals(0, selection.selectedColorIndex(0))
        assertEquals(3, selection.selectedColorIndex(1))
    }

    @Test
    fun selectingSameColorPreventsStarting() {
        val selection = MenuColorSelection()

        selection.selectColor(1, 0)

        assertFalse(selection.canStart)
    }

    @Test
    fun selectingDifferentColorAgainAllowsStarting() {
        val selection = MenuColorSelection()

        selection.selectColor(1, 0)
        assertFalse(selection.canStart)

        selection.selectColor(1, 2)

        assertTrue(selection.canStart)
    }

    @Test
    fun selectedColorReturnsCorrectPaletteValue() {
        val selection = MenuColorSelection()

        assertEquals(0x00E5FF, selection.selectedColor(0))
        assertEquals(0xFF3B3B, selection.selectedColor(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun selectingColorForInvalidPlayerThrowsException() {
        val selection = MenuColorSelection()

        selection.selectColor(2, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun selectingInvalidColorThrowsException() {
        val selection = MenuColorSelection()

        selection.selectColor(0, 100)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyPaletteThrowsException() {
        MenuColorSelection(palette = emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidInitialColorIndexThrowsException() {
        MenuColorSelection(initialColorIndices = listOf(0, 100))
    }
    @Test
    fun eightPlayersCanStartWithDifferentColors() {
        val selection = MenuColorSelection(
            initialColorIndices = (0 until 8).toList()
        )

        assertTrue(selection.canStart)

        for (player in 0 until 8) {
            assertEquals(player, selection.selectedColorIndex(player))
        }
    }

    @Test
    fun duplicateColorAmongEightPlayersPreventsStarting() {
        val selection = MenuColorSelection(
            initialColorIndices = (0 until 8).toList()
        )

        selection.selectColor(7, 0)

        assertFalse(selection.canStart)

        selection.selectColor(7, 7)

        assertTrue(selection.canStart)
    }

    @Test
    fun initialDuplicateColorsPreventStarting() {
        val selection = MenuColorSelection(
            initialColorIndices = listOf(0, 0)
        )

        assertFalse(selection.canStart)
    }

    @Test
    fun paletteIsCopiedOnConstruction() {
        val originalPalette = mutableListOf(0x112233, 0x445566)

        val selection = MenuColorSelection(
            palette = originalPalette,
            initialColorIndices = listOf(0, 1)
        )

        originalPalette[0] = 0xAABBCC
        originalPalette.clear()

        assertEquals(0x112233, selection.selectedColor(0))
        assertEquals(0x445566, selection.selectedColor(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun readingSelectedColorForInvalidPlayerThrowsException() {
        val selection = MenuColorSelection()

        selection.selectedColorIndex(2)
    }
}
