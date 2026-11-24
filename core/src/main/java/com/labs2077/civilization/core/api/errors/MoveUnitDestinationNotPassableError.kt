@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.TerrainDefinitionId
import com.labs2077.civilization.core.models.UnitId

data class MoveUnitDestinationNotPassableError(
    val unitId: UnitId, val destination: Hexagon, val terrainDefinitionId: TerrainDefinitionId,
) : Error("Miejsce docelowe jest nieosiągalne: $unitId (cel = ${destination}, ${terrainDefinitionId}).")
