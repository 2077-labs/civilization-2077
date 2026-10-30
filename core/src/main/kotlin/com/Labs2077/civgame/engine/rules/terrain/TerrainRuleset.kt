package com.Labs2077.civgame.engine.rules.terrain

import com.Labs2077.civgame.engine.rules.AbstractRuleset
import com.Labs2077.civgame.engine.terrain.ProductionEffectMode
import com.Labs2077.civgame.engine.terrain.TerrainDefinition
import com.Labs2077.civgame.engine.terrain.TerrainProductionRule


/**
 * Ruleset przechowujący definicje rodzajów terenu
 *
 * Odpowiada za walidację definicji oraz udostępnianie ich
 * na podstawie identyfikatora.
 */
class TerrainRuleset private constructor(
    schemaVersion: Int,
    terrains: List<TerrainDefinition>
) : AbstractRuleset<TerrainDefinition>(
    schemaVersion = schemaVersion,
    definitions = terrains,
    typeName = "terrains"
) {
    val terrains: List<TerrainDefinition>
        get() = definitions

    companion object {
        /**
         * Sprawdza poprawność pojedynczej definicji terenu
         *
         * @param terrain definicja terenu, która podlega walidacji
         */
        private fun validateDefinition(terrain: TerrainDefinition) {
            require(terrain.id.matches(Regex("[a-z][a-z0-9_]*"))) {
                "Niepoprawne id terenu '${terrain.id}'"
            }

            require(terrain.displayName.isNotBlank()) {
                "${terrain.id}: displayName nie może być puste"
            }

            require(terrain.movementCost > 0) {
                "${terrain.id}: movementCost musi być większy od 0"
            }

            val production = terrain.production

            if (production is TerrainProductionRule.BuildingModifiers) {
                production.modifiers.forEach { modifier ->
                    require(
                        modifier.buildingId.matches(
                            Regex("[a-z][a-z0-9_]*")
                        )
                    ) {
                        "${terrain.id}: niepoprawne id budynku '${modifier.buildingId}'"
                    }
                }

                val replacePriorities = production.modifiers
                    .filter { it.mode == ProductionEffectMode.REPLACE }
                    .map { it.priority }

                require(
                    replacePriorities.distinct().size == replacePriorities.size
                ) {
                    "${terrain.id}: efekty REPLACE muszą mieć różne priorytety"
                }
            }
        }
        /**
         * Tworzy ruleset terenów na podstawie danych odczytanych z pliku JSON.
         *
         * Sprawdza wersję schematu i waliduje wszystkie definicje
         * przed utworzeniem rulesetu.
         *
         * @param file dane konfiguracji terenów odczytane z JSON.
         * @return zwalidowany ruleset zawierający definicje terenów.
         */
        fun fromFile(file: TerrainRulesetFile): TerrainRuleset {
            require(file.schemaVersion == 1) {
                "Nieobsługiwana wersja terrains.json: ${file.schemaVersion}"
            }
            file.terrains.forEach(::validateDefinition)
            return TerrainRuleset(file.schemaVersion, file.terrains)
        }
    }
}
