package com.Labs2077.civgame.client.rendering

import com.labs2077.civilization.core.models.CivilizationId
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.UnitDefinitionId

/** Jednostka w pozycji świata (x, y = środek kafelka, może być między kafelkami podczas ruchu). */
class UnitSprite(
    val civilizationId: CivilizationId,
    val unitTypeId: UnitDefinitionId,
    val x: Float,
    val y: Float,
    val moving: Boolean,
    val flipX: Boolean
)

/**
 * Miasto w pozycji świata (tutaj już pozycja kafelka, ponieważ nie jest dopuszczane
 * poruszanie się miast).
 */
class CitySprite(
    val civilizationId: CivilizationId,
    val level: Int,
    val tile: Hexagon
)
