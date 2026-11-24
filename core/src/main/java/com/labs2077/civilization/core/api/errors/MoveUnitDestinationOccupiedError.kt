@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitId

data class MoveUnitDestinationOccupiedError(
    val unitId: UnitId,
    val destination: Hexagon,

    /** Jednostki, które znajdują się na polu docelowym. */
    val units: Collection<Unit>,
) : Error("Miejsce docelowe jest zajęte: $unitId (cel = $destination, jednostki = $units).")
