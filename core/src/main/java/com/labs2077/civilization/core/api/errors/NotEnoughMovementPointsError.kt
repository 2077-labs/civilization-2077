@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.UnitId

data class NotEnoughMovementPointsError(val unitId: UnitId, val requiredMovementPoints: Double) :
    Error("Jednostka posiada niewystarczającą ilość punków ruchu: $unitId (wymagane = $requiredMovementPoints).")
