package com.Labs2077.civgame.engine.units

import com.Labs2077.civgame.engine.resources.ResourceBundle
import com.Labs2077.civgame.engine.rules.RuleDefinition
import com.Labs2077.civgame.engine.rules.UnitId
import com.Labs2077.civgame.engine.rules.units.BuildingId
import kotlinx.serialization.Serializable

/**
 * Niezmienna definicja typu jednostki pochodząca z rulesetu.
 * id jest stabilnym prefiksem regionów atlasu, np. scout_idle_1.png.
 */
@Serializable
data class UnitDefinition(
    override val id: UnitId,
    val displayName: String,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val movement: Int,
    val vision: Int,
    /** Zasięg ataku w polach. Dla wręcz wynosi 1; atakujący bez zdolności ataku tego nie używa. */
    val attackRange: Int = 1,
    val cost: ResourceBundle = ResourceBundle(),
    val upkeep: ResourceBundle = ResourceBundle(),
    val abilities: Set<UnitAbility> = emptySet(),
    /** Identyfikatory budynków, które muszą być dostępne, aby odblokować rekrutację. */
    val requiredBuildings: List<BuildingId> = emptyList(),
    /** null oznacza brak limitu; limit dotyczy jednego gracza. */
    val limitPerPlayer: Int? = null
) : RuleDefinition<UnitId>
