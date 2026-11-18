package com.labs2077.civilization.core.models

/**
 * Ruleset przechowujący definicje rodzajów terenu.
 */
class TerrainRuleset(definitions: List<TerrainDefinition>) :
    AbstractRuleset<TerrainDefinitionId, TerrainDefinition>(definitions, keySelector = { it.id })
