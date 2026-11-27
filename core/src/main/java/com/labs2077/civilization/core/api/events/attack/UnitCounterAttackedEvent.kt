package com.labs2077.civilization.core.api.events

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.models.UnitId

/** Jednostka [unitId] (obrońca, który przeżył) wykonała kontratak na jednostkę [targetId] (pierwotnego atakującego). */
data class UnitCounterAttackedEvent(val unitId: UnitId, val targetId: UnitId) : Event
