package com.Labs2077.civgame.engine.terrain

import com.Labs2077.civgame.engine.rules.RuleDefinition
import kotlinx.serialization.Serializable
/**
 * Opisuje rodzaj terenu i jego właściwości wykorzystywane w grze
 *
 * @property id unikalny identyfikator terenu, np. "scrapyard"
 * @property displayName nazwa terenu wyświetlana graczowi
 * @property passable określa czy jednostki mogą wejść na ten teren
 * @property movementCost koszt ruchu potrzebny do wejścia na pole
 * @property production reguła określająca produkcję zasobów terenu
 */
@Serializable
data class TerrainDefinition(
    override val id: String,
    val displayName: String,
    val passable: Boolean,
    val movementCost: Double,
    val production: TerrainProductionRule
) : RuleDefinition
