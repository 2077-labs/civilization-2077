package com.Labs2077.civgame.engine.buildings

import com.Labs2077.civgame.engine.resources.ResourceBundle
import com.Labs2077.civgame.engine.rules.RuleDefinition
import com.Labs2077.civgame.engine.rules.units.BuildingId
import kotlinx.serialization.Serializable

/**
 * Definicja budynku pochodząca z rulesetu.
 *
 * @property id unikalny identyfikator budynku
 * @property displayName nazwa wyświetlana budynku
 * @property cost koszt budowy budynku
 * @property requiredBuildings lista wymaganych budynków
 * @property features zestaw funkcjonalności budynku
 */
@Serializable
class BuildingDefinition (
    override val id: BuildingId,
    val displayName: String,
    val cost: ResourceBundle = ResourceBundle(),
    val requiredBuildings: List<BuildingId> = emptyList(),
    val features: Set<BuildingFeature> = emptySet()
) : RuleDefinition<BuildingId> {
    fun hasFeature(feature: BuildingFeature): Boolean = feature in features
}
