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
}
