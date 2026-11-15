package com.Labs2077.civgame.engine.rules.buildings

import com.Labs2077.civgame.engine.buildings.BuildingDefinition
import com.Labs2077.civgame.engine.buildings.BuildingFeature
import com.Labs2077.civgame.engine.rules.AbstractRuleset
import com.Labs2077.civgame.engine.rules.units.BuildingId

/**
 * Ruleset budynków, zawiera definicje budynków w grze.
 */
class BuildingRuleset private constructor(
    schemaVersion: Int,
    buildingDefinitions: List<BuildingDefinition>
): AbstractRuleset<BuildingId, BuildingDefinition>(
    schemaVersion,
    buildingDefinitions,
    typeName = "buildings"
) {
    val buildings: List<BuildingDefinition>
        get() = definitions

    /** Wszyustkie budynki, które mają funkcjonalność [feature]. */
    fun withFeature(feature: BuildingFeature): List<BuildingDefinition> =
        definitions.filter { it.hasFeature(feature) }

    /** Wszystkie funkcjonalności wszystkich podanych budynków. */
    fun featuresOf(buildings: List<BuildingDefinition>): Set<BuildingFeature> =
        buildings.flatMap { it.features }.toSet()

    /** Czy któryś z podanych budynków ma funkcjonalność [feature]. */
    fun hasFeature(buildings: Set<BuildingId>, feature: BuildingFeature): Boolean =
        buildings.any { get(it).hasFeature(feature) }

    /** Czy można zbudować budynek przy posiadanych budynkach [ownedBuildings]. */
    fun isAvailable(id: BuildingId, ownedBuildings: Set<BuildingId>): Boolean =
        id !in ownedBuildings && ownedBuildings.containsAll(get(id).requiredBuildings)

    companion object {
        private val ID_REGEX = Regex("[a-z][a-z0-9_]*")

        fun fromFile(file: BuildingRulesetFile): BuildingRuleset {
            require(file.schemaVersion == 1) {
                "Nieobsługiwana wersja buildings.json: " +
                    file.schemaVersion
            }
            file.buildings.forEach { validateDefinition(it) }
            val ruleset = BuildingRuleset(file.schemaVersion, file.buildings)
            validateRequirements(ruleset.buildings)
            return BuildingRuleset(file.schemaVersion, file.buildings)
        }

        private fun validateDefinition(buildingDefinition: BuildingDefinition) {
            val name = buildingDefinition.id.value
            require(name.matches(ID_REGEX)) {
                "Niepoprawne id budynku '$name' — użyj małych liter, cyfr i '_'"
            }
            require(buildingDefinition.displayName.isNotBlank()) {
                "$name: displayName nie może być puste"
            }
            require(buildingDefinition.cost.isNonNegative()) {
                "$name: koszt nie może być ujemny"
            }
            require(buildingDefinition.requiredBuildings.distinct().size == buildingDefinition.requiredBuildings.size) {
                "$name: wymagania budynków nie mogą się powtarzać"
            }
        }

        private fun validateRequirements(buildings: List<BuildingDefinition>) {
            val byId = buildings.associateBy { it.id }

            buildings.forEach { building ->
                building.requiredBuildings.forEach { requiredId ->
                    require(requiredId in byId) {
                        "${building.id.value}: wymagany budynek '${requiredId.value}' nie istnieje"
                    }
                }
            }

            // wykrywanie cykli w grafie zależności budynków
            val visiting = HashSet<BuildingId>()
            val done = HashSet<BuildingId>()
            fun visit(id: BuildingId, path: List<BuildingId>) {
                if (id in done) return
                require(id !in visiting) {
                    "Cykliczne wymagania budynków: " +
                        (path + id).joinToString(" -> ") { it.value }
                }
                visiting += id
                byId.getValue(id).requiredBuildings.forEach { visit(it, path + id) }
                visiting -= id
                done += id
            }
            buildings.forEach { visit(it.id, emptyList()) }
        }
    }

}
