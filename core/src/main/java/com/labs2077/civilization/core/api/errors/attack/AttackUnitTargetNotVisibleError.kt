@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.UnitId

data class AttackUnitTargetNotVisibleError(val unitId: UnitId, val targetId: UnitId) :
    Error("Cel nie jest widoczny dla cywilizacji atakującego: $unitId (cel = $targetId).")
