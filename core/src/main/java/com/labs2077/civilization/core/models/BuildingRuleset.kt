package com.labs2077.civilization.core.models

/**
 * Ruleset przechowujący definicje rodzajów budynków.
 */
class BuildingRuleset(definitions: List<BuildingDefinition>) :
    AbstractRuleset<BuildingDefinitionId, BuildingDefinition>(definitions, { it.id }) {

    init {
        ensureAcyclicBuildingRequirements()
    }

    /** Odszukuje wszystkie rodzaje budynków, które posiadają wskazaną funkcjonalność. */
    fun findAllWithFeature(feature: BuildingFeature): Collection<BuildingDefinition> {
        return definitions.values.filter { feature in it.features }
    }

    /** Sprawdza, czy zależności budynków nie mają cykli. */
    private fun ensureAcyclicBuildingRequirements() {
        val path = LinkedHashSet<BuildingDefinitionId>()
        val checked = HashSet<BuildingDefinitionId>()

        fun HashSet<BuildingDefinitionId>.joinToPath(): String {
            return joinToString(separator = " -> ") { it.value }
        }

        fun visit(id: BuildingDefinitionId) {
            if (id in checked) {
                return
            }

            require(path.add(id)) {
                "Cykliczne wymagania typów budynków: ${path.joinToPath()} -> $id."
            }

            val definition = definitions[id]

            require(definition != null) {
                "Wymagany typ budynku nie istnieje: ${path.joinToPath()} -> $id."
            }

            definition.requiredBuildingTypeIds.forEach(::visit)

            path.remove(id)
            checked.add(id)
        }

        definitions.keys.forEach(::visit)
    }
}
