package com.labs2077.civilization.core.api.events

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.models.UnitId

data class UnitMovementUpdatedEvent(val unitId: UnitId, val movement: Int) : Event
