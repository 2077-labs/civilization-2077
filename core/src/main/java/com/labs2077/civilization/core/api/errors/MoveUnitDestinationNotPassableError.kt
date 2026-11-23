@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.Terrain
import com.labs2077.civilization.core.models.UnitId

class MoveUnitDestinationNotPassableError(val unitId: UnitId, val terrain: Terrain) :
    Error("Miejsce docelowe jest nieosiągalne: $unitId (cel = ${terrain.position}, ${terrain.definition.id}).")
