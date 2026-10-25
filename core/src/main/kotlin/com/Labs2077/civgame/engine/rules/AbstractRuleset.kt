package com.Labs2077.civgame.engine.rules

/**
 * Klasa bazowa dla rulesetów, która zapewnia podstawową walidację i dostęp do definicji reguł.
 */
abstract class AbstractRuleset<T : RuleDefinition>(
    final override val schemaVersion: Int,
    definitions: List<T>,
    private val typeName: String
) : Ruleset<T> {

    final override val definitions: List<T> = definitions.toList()

    private val byId: Map<String, T> =
        this.definitions.associateBy(RuleDefinition::id)

    init {
        require(schemaVersion > 0) {
            "Wersja schematu musi być dodatnia"
        }

        require(this.definitions.isNotEmpty()) {
            "Ruleset '$typeName' nie zawiera definicji"
        }

        require(this.definitions.all { it.id.isNotBlank() }) {
            "Ruleset '$typeName' zawiera puste ID"
        }

        val duplicates = this.definitions
            .groupingBy(RuleDefinition::id)
            .eachCount()
            .filterValues { it > 1 }
            .keys

        require(duplicates.isEmpty()) {
            "Powtórzone ID w rulesecie '$typeName': " +
                duplicates.joinToString()
        }
    }

    final override operator fun get(id: String): T =
        byId[id] ?: throw NoSuchElementException(
            "Nieznane ID '$id' w rulesecie '$typeName'"
        )

    final override fun find(id: String): T? = byId[id]
}
