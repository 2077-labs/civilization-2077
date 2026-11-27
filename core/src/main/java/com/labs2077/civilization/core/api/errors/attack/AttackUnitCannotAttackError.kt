@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.UnitId

data class AttackUnitCannotAttackError(val unitId: UnitId) :
    Error("Jednostka nie posiada zdolności ataku: $unitId.")
