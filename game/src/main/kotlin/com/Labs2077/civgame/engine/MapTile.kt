package com.Labs2077.civgame.engine

import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.TerrainDefinitionId

/**
 * Reprezentuje pojedyncze pole mapy przechowując jego pozycję i identyfikator terenu.
 */
data class MapTile(
    val position: Hexagon,
    val terrainTypeId: TerrainDefinitionId,
)
