package com.labs2077.civilization.core.api.commands

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.models.UnitId

/** Polecenie ataku jednostki [unitId] na jednostkę [targetId]. */
data class AttackUnitCommand(val unitId: UnitId, val targetId: UnitId) : Command

