@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.UnitId

data class AttackUnitTargetOutOfRangeError(
    val unitId: UnitId,
    val targetId: UnitId,
    val distance: Int,
    val range: Int,
) : Error("Cel znajduje się poza zasięgiem ataku: $unitId (cel = $targetId, odległość = $distance, zasięg = $range).")
