package com.Labs2077.civgame.engine.city

import com.Labs2077.civgame.engine.resources.ResourceBundle
import com.Labs2077.civgame.engine.rules.CityLevelId
import com.Labs2077.civgame.engine.rules.RuleDefinition
import kotlinx.serialization.Serializable

/**
 * Definicja jednego poziomu miasta pochodząca z rulesetu.
 *
 * @property id unikalny identyfikator poziomu
 * @property level numer poziomu (1, 2, 3...)
 * @property centerProduction zasoby generowane co turę przez samo centrum miasta
 * @property levelUpRequirement zasoby potrzebne do awansu z tego poziomu na kolejny.
 *  Jest to próg zgromadzonego zapasu, a nie koszt (nic nie jest zabierane przy awansie). Obecnie liczy się tylko żywność
 * @property radius maksymalny promień (w polach) od centrum, w którym miasto może zajmować pola
 * @property maxTiles maksymalna liczba zajętych pól (nie licząc pola centrum)
 */
@Serializable
data class CityLevelDefinition(
    override val id: CityLevelId,
    val level: Int,
    val centerProduction: ResourceBundle,
    val levelUpRequirement: ResourceBundle? = null,
    val radius: Int,
    val maxTiles: Int,
): RuleDefinition<CityLevelId>