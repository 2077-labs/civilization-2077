
package com.Labs2077.civgame.client.rendering

import com.Labs2077.civgame.client.assets.AssetProvider
import com.labs2077.civilization.core.models.TerrainDefinitionId
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Matrix4
import kotlin.math.sqrt

class HexMenuBackground(
    private val assets: AssetProvider
) {

    private data class BackgroundHex(
        val region: PolygonRegion,
        val x: Float,
        val y: Float
    )

    private val batch = PolygonSpriteBatch()

    private val hexSize = 88f
    private val tileWidth = sqrt(3f) * hexSize
    private val tileHeight = 2f * hexSize

    // Dostępne tekstury terenu z common.atlas.
    private val terrainRegions = listOf(
        "city_ruins",
        "crystal_mountains",
        "deep_water",
        "desert_terrain",
        "radiation_zone",
        "scrap_field",
        "shallow_water",
        "toxic_swamp"
    ).map { terrainId ->
        val texture = assets.terrain(TerrainDefinitionId(terrainId))

        PolygonRegion(
            texture,
            createHexVertices(texture),
            TRIANGLES
        )
    }

    // Losujemy tekstury i pozycje heksów tylko raz.
    private val hexes = buildList {
        for (r in -4..4) {
            for (q in -8..8) {

                val centerX = 640f + tileWidth * (q + r / 2f)
                val centerY = 360f + 1.5f * hexSize * r

                val region = terrainRegions.random()

                add(
                    BackgroundHex(
                        region = region,
                        x = centerX - tileWidth / 2f,
                        y = centerY - tileHeight / 2f
                    )
                )
            }
        }
    }

    fun render(projectionMatrix: Matrix4, alpha: Float) {
        batch.projectionMatrix = projectionMatrix

        batch.setColor(
            0.60f,
            0.72f,
            0.86f,
            0.65f * alpha
        )

        batch.begin()

        try {
            hexes.forEach { hex ->
                batch.draw(
                    hex.region,
                    hex.x,
                    hex.y,
                    tileWidth,
                    tileHeight
                )
            }
        } finally {
            batch.end()
            batch.setColor(Color.WHITE)
        }
    }

    private fun createHexVertices(texture: TextureRegion): FloatArray {
        val width = texture.regionWidth.toFloat()
        val height = texture.regionHeight.toFloat()

        return floatArrayOf(
            width / 2f, height,
            width, height * 0.75f,
            width, height * 0.25f,
            width / 2f, 0f,
            0f, height * 0.25f,
            0f, height * 0.75f
        )
    }

    fun dispose() {
        batch.dispose()
    }

    private companion object {
        val TRIANGLES = shortArrayOf(
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 5
        )
    }
}
