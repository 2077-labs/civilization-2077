package com.Labs2077.civgame.data.repository.rulesets

import com.Labs2077.civgame.data.CityLevelDefinitionRepository
import com.Labs2077.civgame.data.repository.AbstractGdxRepository
import com.Labs2077.civgame.data.repository.rulesets.dto.CityLevelDefinitionFileDto
import com.Labs2077.civgame.data.repository.rulesets.dto.CityLevelDefinitionFileV1Dto
import com.Labs2077.civgame.data.repository.rulesets.dto.toDomainCityLevelDefinitions
import com.labs2077.civilization.core.models.CityLevelDefinition

class CityLevelDefinitionGdxFilesRepository(val path: String) : AbstractGdxRepository(),
    CityLevelDefinitionRepository {

    override fun getAll(): List<CityLevelDefinition> {
        return when (val result = decode<CityLevelDefinitionFileDto>(path)) {
            is CityLevelDefinitionFileV1Dto -> result.levels.toDomainCityLevelDefinitions()
        }
    }
}
