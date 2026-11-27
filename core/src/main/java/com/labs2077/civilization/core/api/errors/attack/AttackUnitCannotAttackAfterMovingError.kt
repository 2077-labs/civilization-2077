@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.UnitId

data class AttackUnitCannotAttackAfterMovingError(val unitId: UnitId) :
    Error("Jednostka nie może atakować w turze, w której się poruszyła: $unitId.")
