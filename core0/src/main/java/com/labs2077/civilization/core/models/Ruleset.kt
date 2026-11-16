package com.labs2077.civilization.core.models

import com.labs2077.civilization.core.polyfill.allUniqueBy

interface Ruleset<K, V> {
    val definitions: Map<K, V>
}

abstract class AbstractRuleset<K, V>(
    definitions: List<V>, keySelector: (V) -> K
) : Ruleset<K, V> {

    init {
        require(definitions.isNotEmpty()) {
            "Ruleset musi zawierać definicje: $definitions."
        }
        require(definitions.allUniqueBy(keySelector)) {
            "Ruleset zawiera zduplikowane definicje: $definitions."
        }
    }

    override val definitions = definitions.associateBy(keySelector)
}
