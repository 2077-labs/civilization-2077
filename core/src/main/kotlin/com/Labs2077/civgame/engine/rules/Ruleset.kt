package com.Labs2077.civgame.engine.rules

/** Każda z definicji w rulesecie musi mieć unikalne ID, które jest używane w wielu miejscach (walidacja reguł,
// wczytywanie grafik, itp.) */
interface RuleDefinition {
    val id: String
}

/**
 * Ruleset jest zbiorem definicji reguł, które są używane w grze.
 */
interface Ruleset<T : RuleDefinition> {
    val schemaVersion: Int
    val definitions: List<T>

    operator fun get(id: String): T
    fun find(id: String): T?
}
