package com.labs2077.civilization.core.api.events

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.UnitId

/**
 * Jednostka [unitId] została zniszczona i usunięta z gry.
 *
 * @param position pole, na którym jednostka została zniszczona (jednostka nie istnieje już w stanie gry).
 * @param destroyedBy jednostka, która zadała śmiertelne obrażenia.
 */
data class UnitDestroyedEvent(val unitId: UnitId, val position: Hexagon, val destroyedBy: UnitId) : Event
