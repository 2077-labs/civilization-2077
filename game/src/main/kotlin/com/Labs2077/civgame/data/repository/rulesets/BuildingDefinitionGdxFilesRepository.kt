package com.Labs2077.civgame.data.repository.rulesets

import com.Labs2077.civgame.data.BuildingDefinitionRepository
import com.Labs2077.civgame.data.repository.AbstractGdxRepository
import com.Labs2077.civgame.data.repository.rulesets.dto.BuildingDefinitionFileDto
import com.Labs2077.civgame.data.repository.rulesets.dto.BuildingDefinitionFileV1Dto
import com.Labs2077.civgame.data.repository.rulesets.dto.toDomainBuildingDefinitions
import com.labs2077.civilization.core.models.BuildingDefinition

class BuildingDefinitionGdxFilesRepository(val path: String) : AbstractGdxRepository(),
    BuildingDefinitionRepository {

    override fun getAll(): List<BuildingDefinition> {
        return when (val result = decode<BuildingDefinitionFileDto>(path)) {
            is BuildingDefinitionFileV1Dto -> result.buildings.toDomainBuildingDefinitions()
        }
    }
}
