@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.UnitId

class NotEnoughMovementPointsError(val unitId: UnitId, val requiredMovementPoints: Int) :
    Error("Jednostka posiada niewystarczającą ilość punków ruchu: $unitId (wymagane = $requiredMovementPoints).")
