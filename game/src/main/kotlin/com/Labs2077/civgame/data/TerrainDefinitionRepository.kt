package com.Labs2077.civgame.data

import com.labs2077.civilization.core.models.TerrainDefinition

interface TerrainDefinitionRepository {
    fun getAll(): List<TerrainDefinition>
}
