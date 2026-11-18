package com.Labs2077.civgame.data

import com.labs2077.civilization.core.models.BuildingDefinition

interface BuildingDefinitionRepository {
    fun getAll(): List<BuildingDefinition>
}
