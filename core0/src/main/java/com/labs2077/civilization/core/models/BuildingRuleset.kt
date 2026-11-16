package com.labs2077.civilization.core.models

/**
 * Ruleset przechowujący definicje rodzajów budynków.
 */
class BuildingRuleset(definitions: List<BuildingDefinition>) :
    AbstractRuleset<BuildingDefinitionId, BuildingDefinition>(definitions, { it.id }) {

    /** Odszukuje wszystkie rodzaje budynków, które posiadają wskazaną funkcjonalność. */
    fun findAllWithFeature(feature: BuildingFeature): List<BuildingDefinition> {
        return definitions.values.filter { feature in it.features }
    }

    /** Sprawdza, czy zależności budynków nie mają cykli. */
    private fun ensureAcyclicBuildingRequirements() {
        // wykrywanie cykli w grafie zależności budynków
        val visiting = HashSet<BuildingDefinitionId>()
        val done = HashSet<BuildingDefinitionId>()

        fun visit(id: BuildingDefinitionId, path: List<BuildingDefinitionId>) {
            if (id in done) return
            require(id !in visiting) {
                "Cykliczne wymagania budynków: " + (path + id).joinToString(" -> ") { it.value }
            }
            visiting += id
            definitions.getValue(id).requiredBuildingTypes.forEach { visit(it.id, path + id) }
            visiting -= id
            done += id
        }

        definitions.forEach { visit(it.key, emptyList()) }
    }
}
