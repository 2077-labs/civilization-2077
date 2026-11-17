package com.Labs2077.civgame.engine.rules.city

import com.Labs2077.civgame.engine.city.CityLevelDefinition
import com.Labs2077.civgame.engine.rules.AbstractRuleset
import com.Labs2077.civgame.engine.rules.CityLevelId

class CityLevelRuleset private constructor(
    schemaVersion: Int,
    levelDefinitions: List<CityLevelDefinition>
) : AbstractRuleset<CityLevelId, CityLevelDefinition>(
    schemaVersion = schemaVersion,
    definitions = levelDefinitions,
    typeName = "city_levels"
) {
    /** Poziomy posortowane rosnąco po numerze poziomu */
    val levels: List<CityLevelDefinition> = definitions.sortedBy { it.level }

    /** Poziom startowy (nowo założone miasto) */
    val initial: CityLevelDefinition get() = levels.first()

    val maxLevel: Int get() = levels.last().level

    fun byLevel(level: Int): CityLevelDefinition? = levels.find { it.level == level }

    /** Następny poziom lub null, jeśli obecny poziom jest maksymalny */
    fun next(id: CityLevelId): CityLevelDefinition? = byLevel(get(id).level + 1)

    companion object {
        private val ID_REGEX = Regex("[a-z][a-z0-9_]*")

        fun fromFile(file: CityLevelRulesetFile): CityLevelRuleset {
            require(file.schemaVersion == 1) { "Nieobsługiwana wersja schematu rulesetu poziomów miasta: ${file.schemaVersion}" }
            val levels = file.levels
            levels.forEach { validateDefinition(it) }
            val ruleset = CityLevelRuleset(
                schemaVersion = file.schemaVersion,
                levelDefinitions = levels
            )
            validateProgression(ruleset.levels)
            return ruleset
        }

        private fun validateDefinition(level: CityLevelDefinition) {
            val name = level.id.value
            require(name.matches(ID_REGEX)) {
                "Niepoprawne id poziomu miasta '$name' — użyj małych liter, cyfr i '_'"
            }
            require(level.level >= 1) { "$name: level musi być >= 1" }
            require(level.centerProduction.isNonNegative()) { "$name: centerProduction nie może być ujemne" }
            require(level.radius >= 1) { "$name: radius musi być >= 1" }
            require(level.maxTiles >= 1) { "$name: maxTiles musi być >= 1" }

            // liczba pól w promieniu r (bez centrum) na siatce heksagonalnej to 3r(r+1)
            val capacity = 3 * level.radius * (level.radius + 1)
            require(level.maxTiles <= capacity) {
                "$name: maxTiles (${level.maxTiles}) przekracza liczbę pól w promieniu ${level.radius} ($capacity)"
            }

            level.levelUpRequirement?.let { requirement ->
                require(requirement.isNonNegative()) { "$name: levelUpRequirement nie może być ujemne" }
                require(requirement.food > 0) { "$name: levelUpRequirement.food musi być > 0" }
                require(requirement.metals == 0 && requirement.crystals == 0) {
                    "$name: awans zależy tylko od żywności"
                }
            }
        }

        private fun validateProgression(sorted: List<CityLevelDefinition>) {
            sorted.forEachIndexed { index, level ->
                require(level.level == index + 1) {
                    "Poziomy miasta muszą mieć kolejne numery od 1; oczekiwano ${index + 1}, jest ${level.level} (${level.id.value})"
                }
            }
            sorted.dropLast(1).forEach {
                require(it.levelUpRequirement != null) {
                    "${it.id.value}: brak levelUpRequirement, a istnieje wyższy poziom"
                }
            }
            require(sorted.last().levelUpRequirement == null) {
                "${sorted.last().id.value}: najwyższy poziom nie może mieć levelUpRequirement"
            }
            sorted.zipWithNext().forEach { (lower, higher) ->
                require(higher.radius >= lower.radius) {
                    "${higher.id.value}: radius nie może maleć"
                }
                require(higher.maxTiles >= lower.maxTiles) {
                    "${higher.id.value}: maxTiles nie może maleć"
                }
            }
            val thresholds = sorted.mapNotNull { it.levelUpRequirement?.food }
            require(thresholds.zipWithNext().all { (a, b) -> b > a }) {
                "Progi żywności potrzebne do awansu muszą rosnąć z poziomem"
            }
        }
    }
}