@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.UnitId

data class AttackUnitTargetIsFriendlyError(val unitId: UnitId, val targetId: UnitId) :
    Error("Jednostka nie może zaatakować jednostki własnej cywilizacji: $unitId (cel = $targetId).")
