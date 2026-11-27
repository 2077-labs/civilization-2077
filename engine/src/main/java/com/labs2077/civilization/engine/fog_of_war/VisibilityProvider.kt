package com.labs2077.civilization.engine.fog_of_war

import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.Hexagon

/**
 * Określa, czy dane pole jest widoczne (i odkryte) dla cywilizacji.
 *
 * Abstrakcja istnieje po to, żeby po wprowadzeniu mgły wojny wystarczyło podmienić implementację,
 * bez ruszania obsługi ataku.
 */
internal fun interface VisibilityProvider {
    fun isVisible(civilization: Civilization, position: Hexagon): Boolean
}

/**
 * TYMCZASOWA implementacja do czasu wprowadzenia mgły wojny.
 *
 * - Widoczność: pole jest widoczne, jeśli leży w zasięgu widzenia ([vision][com.labs2077.civilization.core.models.UnitDefinition.vision])
 *   któregokolwiek z żywych jednostek cywilizacji. Dzięki temu np. artyleria (zasięg 3, widzenie 2) potrzebuje zwiadowcy.
 * - Odkrycie: na sztywno zakładamy, że każde pole jest odkryte (brak mapy odkrytych pól).
 * - TODO: Miasta nie dają jeszcze widoczności (ale i tak cała ta mechanika powinna zostać przebudowana).
 */
internal object UnitVisibilityProvider : VisibilityProvider {
    override fun isVisible(civilization: Civilization, position: Hexagon): Boolean {
        return civilization.units.any { unit ->
            unit.position.distanceTo(position) <= unit.definition.vision
        }
    }
}
