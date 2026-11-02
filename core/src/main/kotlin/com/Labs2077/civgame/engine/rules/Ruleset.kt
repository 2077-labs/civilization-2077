package com.Labs2077.civgame.engine.rules

/** Każda z definicji w rulesecie musi mieć unikalne ID, które jest używane w wielu miejscach (walidacja reguł,
// wczytywanie grafik, itp.) */
interface RuleDefinition<ID : RuleId> {
    val id: ID
}

/**
 * Ruleset jest zbiorem definicji reguł, które są używane w grze.
 */
interface Ruleset<ID : RuleId, T : RuleDefinition<ID>> {
    val schemaVersion: Int
    val definitions: List<T>

    operator fun get(id: ID): T
    fun find(id: ID): T?
}
