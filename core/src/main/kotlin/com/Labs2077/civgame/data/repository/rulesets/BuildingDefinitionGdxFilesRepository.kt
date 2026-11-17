package com.Labs2077.civgame.data.repository.rulesets

import com.Labs2077.civgame.data.BuildingDefinitionRepository
import com.Labs2077.civgame.data.repository.AbstractGdxRepository
import com.Labs2077.civgame.data.repository.rulesets.dto.BuildingDefinitionFileDto
import com.Labs2077.civgame.data.repository.rulesets.dto.BuildingDefinitionFileV1Dto
import com.Labs2077.civgame.data.repository.rulesets.dto.toDomain
import com.labs2077.civilization.core.models.BuildingDefinition
import kotlinx.serialization.json.Json

class BuildingDefinitionGdxFilesRepository(val path: String) : AbstractGdxRepository(),
    BuildingDefinitionRepository {

    override val configuration = Json {
        classDiscriminator = "schemaVersion"
        ignoreUnknownKeys = false
        isLenient = false
    }

    override fun getAll(): List<BuildingDefinition> {
        return when (val result = decode<BuildingDefinitionFileDto>(path)) {
            is BuildingDefinitionFileV1Dto -> result.buildings.toDomain()
        }
    }
}
