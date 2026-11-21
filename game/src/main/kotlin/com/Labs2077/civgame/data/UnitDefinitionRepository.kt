package com.Labs2077.civgame.data

import com.labs2077.civilization.core.models.UnitDefinition

interface UnitDefinitionRepository {
    fun getAll(): List<UnitDefinition>
}
