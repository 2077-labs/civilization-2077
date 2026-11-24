package com.labs2077.civilization.core.api.events

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.UnitId

data class UnitMovedEvent(val unitId: UnitId, val source: Hexagon, val destination: Hexagon) : Event
