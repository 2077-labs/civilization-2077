package com.Labs2077.civgame.client.rendering

import com.badlogic.gdx.math.Vector2
import com.labs2077.civilization.core.models.Hexagon
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class HexLayoutTest {

    @Test
    fun axialToWorld_shouldConvertOriginCorrectly() {
        val result = HexLayout.axialToWorld(
            Hexagon(0, 0),
            3f,
            Vector2()
        )

        assertEquals(0f, result.x, 0.0001f)
        assertEquals(0f, result.y, 0.0001f)
    }

    @Test
    fun axialToWorld_shouldConvertCoordinatesCorrectly() {
        val result = HexLayout.axialToWorld(
            Hexagon(1, 0),
            3f,
            Vector2()
        )

        assertEquals(sqrt(3f) * 3f, result.x, 0.0001f)
        assertEquals(0f, result.y, 0.0001f)
    }

    @Test
    fun worldToAxial_shouldRestoreOriginalCoordinates() {
        val coordinates = listOf(
            Hexagon(0, 0),
            Hexagon(1, 0),
            Hexagon(0, 1),
            Hexagon(-2, 3),
            Hexagon(4, -1),
            Hexagon(-3, -2)
        )

        val hexSize = 3f

        for (coordinate in coordinates) {
            val world = HexLayout.axialToWorld(
                coordinate,
                hexSize,
                Vector2()
            )

            val restored = HexLayout.worldToAxial(
                world.x,
                world.y,
                hexSize
            )

            assertEquals(coordinate, restored)
        }
    }

    @Test
    fun worldToAxial_shouldReturnNearestHexForSmallOffset() {
        val expected = Hexagon(2, -1)
        val hexSize = 3f

        val center = HexLayout.axialToWorld(
            expected,
            hexSize,
            Vector2()
        )

        val result = HexLayout.worldToAxial(
            center.x + hexSize * 0.2f,
            center.y - hexSize * 0.1f,
            hexSize
        )

        assertEquals(expected, result)
    }
}
