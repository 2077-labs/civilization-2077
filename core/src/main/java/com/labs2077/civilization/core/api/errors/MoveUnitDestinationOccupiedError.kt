@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.UnitId

class MoveUnitDestinationOccupiedError(val unitId: UnitId, val destination: Hexagon) :
    Error("Miejsce docelowe jest zajęte: $unitId (cel = $destination).")
