package com.Labs2077.civgame.data.repository.rulesets

import com.Labs2077.civgame.data.UnitDefinitionRepository
import com.Labs2077.civgame.data.repository.AbstractGdxRepository
import com.Labs2077.civgame.data.repository.rulesets.dto.UnitDefinitionFileDto
import com.Labs2077.civgame.data.repository.rulesets.dto.UnitDefinitionFileV1Dto
import com.Labs2077.civgame.data.repository.rulesets.dto.toDomainUnitDefinitions
import com.labs2077.civilization.core.models.UnitDefinition

class UnitDefinitionGdxFilesRepository(val path: String) : AbstractGdxRepository(),
    UnitDefinitionRepository {

    override fun getAll(): List<UnitDefinition> {
        return when (val result = decode<UnitDefinitionFileDto>(path)) {
            is UnitDefinitionFileV1Dto -> result.units.toDomainUnitDefinitions()
        }
    }
}
