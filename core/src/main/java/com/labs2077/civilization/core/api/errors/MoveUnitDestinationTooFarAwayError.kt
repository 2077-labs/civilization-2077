@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.UnitId

data class MoveUnitDestinationTooFarAwayError(val unitId: UnitId, val destination: Hexagon) :
    Error("Miejsce docelowe znajduje się zbyt daleko jednostki: $unitId (cel = $destination).")
