package com.Labs2077.civgame.engine.terrain

import com.Labs2077.civgame.engine.resources.ResourceBundle
import com.Labs2077.civgame.engine.rules.units.BuildingId
import kotlinx.serialization.Serializable

/**
 * Opisuje pojedynczy efekt budynku wpływający na produkcję terenu
 *
 * @property buildingId identyfikator budynku aktywującego efekt
 * @property mode określa, czy efekt dodaje zasoby, czy zastępuje produkcję bazową.
 * @property resources wartości zasobów związane z efektem.
 * @property priority priorytet efektu REPLACE; większa wartość oznacza pierwszeństwo.
 */
@Serializable
data class BuildingProductionModifier(
    val buildingId: BuildingId,
    val mode: ProductionEffectMode,
    val resources: ResourceBundle,
    val priority: Int = 0
)
