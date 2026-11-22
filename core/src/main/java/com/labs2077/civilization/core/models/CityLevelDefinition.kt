package com.labs2077.civilization.core.models

@JvmInline
value class CityLevelDefinitionId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) {
            "Identyfikator definicji poziomu miasta nie może być pusty."
        }
    }
}

/**
 * Definicja poziomu miasta.
 *
 * @property id unikalny identyfikator poziomu
 * @property level numer poziomu (1, 2, 3...)
 * @property centerProduction zasoby generowane co turę przez samo centrum miasta
 * @property levelUpRequirement zasoby potrzebne do awansu z tego poziomu na kolejny.
 *  Jest to próg zgromadzonego zapasu, a nie koszt (nic nie jest zabierane przy awansie). Obecnie liczy się tylko żywność
 * @property maximumCityRadius maksymalny promień (w polach) od centrum, w którym miasto może zajmować pola
 * @property maximumTerritorySize maksymalna liczba zajętych pól (nie licząc pola centrum)
 */
data class CityLevelDefinition(
    val id: CityLevelDefinitionId,
    val level: Int,
    val centerProduction: Resources,
    val levelUpRequirement: CityResources? = null,
    val maximumCityRadius: Int,
    val maximumTerritorySize: Int,
) {
    init {
        require(level >= 1) {
            "Numer poziomu (level) musi być liczbą dodatnią: $level."
        }
        require(!centerProduction.isNegative) {
            "Ilość zasobów generowanych przez centrum miasta (centerProduction) nie może być ujemna: $centerProduction."
        }
        require(maximumCityRadius >= 1) {
            "Maksymalny promień miasta (maximumCityRadius) musi być liczbą dodatnią: $maximumCityRadius."
        }
        require(maximumTerritorySize >= 1) {
            "Maksymalna liczba zajętych pól (maximumTerritorySize) musi być liczbą dodatnią: $maximumTerritorySize."
        }

        // Pierścienie wokół środkowego heksagonu składają się z 6R heksagonów.
        // Suma wszystkich takich pierścieni w promieniu `R` (wraz ze środkiem) wynosi 1 + 3R * (R + 1).
        val capacity = 1 + (3 * maximumCityRadius) * (maximumCityRadius + 1)

        require(maximumTerritorySize <= capacity) {
            "Maksymalna liczba zajętych pól (maximumTerritorySize) przekracza liczbę heksagonów w promieniu $maximumCityRadius: $maximumTerritorySize (maksymalnie $capacity)."
        }

        levelUpRequirement?.let {
            require(!it.isNegative) {
                "Ilość zasobów potrzebnych do awansu miasta (levelUpRequirement) nie może być ujemna: $it."
            }
        }
    }
}
