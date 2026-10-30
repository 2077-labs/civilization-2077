package com.Labs2077.civgame.engine.map

/**
 * Reprezentuje pojedyncze pole mapy przechowując jego pozycję i identyfikator terenu.
 */
data class MapTile(
    val position: MapCoordinate,
    val terrainId: String
)
