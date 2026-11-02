package com.Labs2077.civgame.engine.rules

/**
 * Klasa bazowa dla rulesetów, która zapewnia podstawową walidację i dostęp do definicji reguł.
 */
abstract class AbstractRuleset<ID : RuleId, T : RuleDefinition<ID>>(
    final override val schemaVersion: Int,
    definitions: List<T>,
    private val typeName: String
) : Ruleset<ID, T> {

    final override val definitions: List<T> = definitions.toList()
    private val byId: Map<ID, T> =
        this.definitions.associateBy { it.id }

    init {
        require(schemaVersion > 0) {
            "Wersja schematu musi być dodatnia"
        }

        require(this.definitions.isNotEmpty()) {
            "Ruleset '$typeName' nie zawiera definicji"
        }

        require(this.definitions.all { it.id.value.isNotBlank() }) {
            "Ruleset '$typeName' zawiera puste ID"
        }

        val duplicates = this.definitions
            .groupingBy { it.id }
            .eachCount()
            .filterValues { it > 1 }
            .keys

        require(duplicates.isEmpty()) {
            "Powtórzone ID w rulesecie '$typeName': " +
                duplicates.joinToString { it.value }
        }
    }

    final override operator fun get(id: ID): T =
        byId[id] ?: throw NoSuchElementException(
            "Nieznane ID '${id.value}' w rulesecie '$typeName'"
        )

    final override fun find(id: ID): T? = byId[id]
}
