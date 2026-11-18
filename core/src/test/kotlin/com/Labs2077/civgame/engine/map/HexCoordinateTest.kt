package com.Labs2077.civgame.engine.map

import com.labs2077.civilization.core.models.Hexagon
import org.junit.Assert.assertEquals
import org.junit.Test

class HexCoordinateTest {

    @Test
    fun neighbors_shouldReturnSixUniqueCoordinates() {
        val center = Hexagon(0, 0)

        val neighbors = center.neighbors()

        assertEquals(6, neighbors.size)
        assertEquals(6, neighbors.toSet().size)
    }

    @Test
    fun neighbors_shouldHaveDistanceOne() {
        val center = Hexagon(0, 0)

        for (neighbor in center.neighbors()) {
            assertEquals(1, center.distanceTo(neighbor))
        }
    }

    @Test
    fun distanceTo_shouldCalculateCorrectDistance() {
        val first = Hexagon(0, 0)
        val second = Hexagon(3, -2)

        assertEquals(0, first.distanceTo(first))
        assertEquals(3, first.distanceTo(second))

        assertEquals(
            first.distanceTo(second), second.distanceTo(first)
        )
    }
}
