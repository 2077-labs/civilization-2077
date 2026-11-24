package com.labs2077.civilization.core.api.commands

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.UnitId

data class MoveUnitCommand(val unitId: UnitId, val destination: Hexagon) : Command
