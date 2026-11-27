package com.labs2077.civilization.core.api.events

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.models.UnitId

/** Jednostka [unitId] wykonała atak na jednostkę [targetId]. Obrażenia są opisane osobnymi zdarzeniami. */
data class UnitAttackedEvent(val unitId: UnitId, val targetId: UnitId) : Event
