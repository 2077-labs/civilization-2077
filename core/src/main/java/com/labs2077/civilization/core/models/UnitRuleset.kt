package com.labs2077.civilization.core.models

/**
 * Ruleset przechowujący definicje rodzajów jednostek.
 */
class UnitRuleset(definitions: List<UnitDefinition>) :
    AbstractRuleset<UnitDefinitionId, UnitDefinition>(definitions, keySelector = { it.id })
