package com.labs2077.civilization.core.api.events

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.models.UnitId

/**
 * Jednostka [unitId] otrzymała obrażenia.
 *
 * @param damage faktycznie zadane obrażenia (nie większe niż punkty życia sprzed ataku).
 * @param health punkty życia, które jednostce pozostały.
 */
data class UnitDamagedEvent(val unitId: UnitId, val damage: Int, val health: Int) : Event
