package com.labs2077.civilization.core.models

class CityLevelRuleset(definitions: List<CityLevelDefinition>) :
    AbstractRuleset<CityLevelDefinitionId, CityLevelDefinition>(
        definitions, keySelector = { it.id }) {

    val levels = definitions.sortedBy { it.level }

    val initial: CityLevelDefinition get() = levels.first()

    init {
        ensureValidProgression()
    }

    fun findNextLevel(definition: CityLevelDefinition): CityLevelDefinition? {
        return levels.find { it.level == definition.level + 1 }
    }

    private fun ensureValidProgression() {
        require(levels.isNotEmpty()) {
            "Nie zdefiniowano żadnych poziomów miast."
        }
        levels.forEachIndexed { index, definition ->
            require(definition.level == index + 1) {
                "Poziomy miasta muszą mieć kolejne numery od 1; oczekiwano ${index + 1}, jest ${definition.level} (${definition.id.value})"
            }
        }
        levels.dropLast(1)
            .forEach { definition ->
                require(definition.levelUpRequirement != null) {
                    "Wszystkie poziomy za wyjątkiem najwyższego muszą posiadać informację o zasobach wymaganych do awansu miasta: ${definition.id.value}."
                }
            }
        levels.last()
            .let { last ->
                require(last.levelUpRequirement == null) {
                    "Najwyższy poziom nie może posiadać informacji o zasobach wymaganych do awansu miasta: ${last.id.value}."
                }
            }
        levels.zipWithNext()
            .forEach { (lower, higher) ->
                require(lower.maximumCityRadius <= higher.maximumCityRadius) {
                    "Maksymalny promień miasta nie może maleć: ${higher.id.value}."
                }
                require(lower.maximumTerritorySize <= higher.maximumTerritorySize) {
                    "Maksymalna liczba zajętych pól nie może maleć: ${higher.id.value}."
                }
            }

        require(levels.mapNotNull { it.levelUpRequirement }
            .zipWithNext()
            .all { (a, b) -> a < b }) {
            "Ilość zasobów potrzebnych do awansu miasta (levelUpRequirement) musi rosnąć wraz z poziomem."
        }
    }
}
