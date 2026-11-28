package com.Labs2077.civgame.client.rendering

import com.labs2077.civilization.core.models.City
import com.labs2077.civilization.core.models.CivilizationId
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.UnitDefinitionId

/** Jednostka w pozycji świata, która podczas ruchu może znajdować się między kafelkami */
class UnitSprite(
    val civilizationId: CivilizationId,
    val unitTypeId: UnitDefinitionId,
    val x: Float,
    val y: Float,
    val moving: Boolean,
    val flipX: Boolean
)

/** Udostępnia rendererowi dane miasta potrzebne do jego wyświetlenia */
class CitySprite(
    val city: City
) {
    val civilizationId: CivilizationId
        get() = city.civilization.id

    val level: Int
        get() = city.level.level

    val tile: Hexagon
        get() = city.position
}
