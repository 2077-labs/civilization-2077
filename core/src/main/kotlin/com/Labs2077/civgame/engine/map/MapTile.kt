package com.Labs2077.civgame.engine.map

import com.Labs2077.civgame.engine.rules.TerrainId

/**
 * Reprezentuje pojedyncze pole mapy przechowując jego pozycję i identyfikator terenu.
 */
data class MapTile(
    val position: HexCoordinate,
    val terrainId: TerrainId
)
