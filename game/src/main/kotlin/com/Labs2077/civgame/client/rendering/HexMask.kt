package com.Labs2077.civgame.client.rendering

import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Klasa do tworzenia maski hexagonalnej dla regionu tekstury, tak aby wyświetlana była jedynie tekstura
 * o kształcie heksagonu. Dodatkowo dla poprawy wydajności, wyniki są buforowane w pamięci podręcznej.
 */
object HexMask {
    private val cache = HashMap<TextureRegion, PolygonRegion>()

    /** Wpisany w region hex pointy-top. */
    fun of(region: TextureRegion): PolygonRegion = cache.getOrPut(region) {
        val w = region.regionWidth.toFloat()
        val h = region.regionHeight.toFloat()

        // środek tekstury
        val cx = w / 2f
        val cy = h / 2f

        // promień to połowa wysokości hexa z uwagi na hexa pointy-top
        val r = h / 2f

        // 12 wpisów dla wierzchołków, ponieważ każdy wierzchołek ma współrzędne x i y i są określane przez 2 kolejne wartości w tablicy
        val vertices = FloatArray(12)
        for (i in 0 until 6) {
            // Wyznaczamy kolejne wierzchołki heksagonu, korzystając z zapisywania parametrycznego okręgu.
            // Parzyste indeksy w tablicy vertices odpowiadają współrzędnym x, a nieparzyste współrzędnym y.
            val angle = (PI / 2.0 + i * PI / 3.0).toFloat()
            vertices[i * 2] = cx + r * cos(angle)
            vertices[i * 2 + 1] = cy + r * sin(angle)
        }
        // Rozbijamy heksagon na trójkąty, aby zostały wyrenderowane przez GPU.
        val triangles = shortArrayOf(
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 5
        )
        PolygonRegion(region, vertices, triangles)
    }

    fun clear() = cache.clear()
}