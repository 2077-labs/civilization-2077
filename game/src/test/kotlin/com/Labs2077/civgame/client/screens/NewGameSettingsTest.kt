package com.Labs2077.civgame.client.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewGameSettingsTest {

    @Test
    fun defaultSettingsShouldBeCorrect() {
        val settings = NewGameSettings()

        assertEquals(WorldSizeOption.MEDIUM, settings.worldSize)
        assertNull(settings.seed)
        assertEquals(2, settings.playerCount)
    }

    @Test
    fun customSettingsShouldPreserveSelectedValues() {
        val settings = NewGameSettings(
            worldSize = WorldSizeOption.LARGE,
            seed = 12345L,
            playerCount = 2
        )

        assertEquals(WorldSizeOption.LARGE, settings.worldSize)
        assertEquals(12345L, settings.seed)
        assertEquals(2, settings.playerCount)
    }

    @Test
    fun onlyTwoPlayersShouldBeAccepted() {
        val settings = NewGameSettings(playerCount = 2)

        assertEquals(2, settings.playerCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun playerCountBelowMinimumShouldBeRejected() {
        NewGameSettings(playerCount = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun playerCountAboveMaximumShouldBeRejected() {
        NewGameSettings(playerCount = 3)
    }

    @Test
    fun worldSizesShouldHaveCorrectDimensions() {
        assertEquals(10, WorldSizeOption.SMALL.width)
        assertEquals(10, WorldSizeOption.SMALL.height)

        assertEquals(20, WorldSizeOption.MEDIUM.width)
        assertEquals(20, WorldSizeOption.MEDIUM.height)

        assertEquals(30, WorldSizeOption.LARGE.width)
        assertEquals(30, WorldSizeOption.LARGE.height)
    }
}
