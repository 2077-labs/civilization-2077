package com.Labs2077.civgame.data

import com.labs2077.civilization.core.models.CityLevelDefinition

interface CityLevelDefinitionRepository {
    fun getAll(): List<CityLevelDefinition>
}
