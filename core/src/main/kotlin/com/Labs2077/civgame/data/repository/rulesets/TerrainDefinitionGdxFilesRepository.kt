package com.Labs2077.civgame.data.repository.rulesets

import com.Labs2077.civgame.data.TerrainDefinitionRepository
import com.Labs2077.civgame.data.repository.AbstractGdxRepository
import com.Labs2077.civgame.data.repository.rulesets.dto.TerrainDefinitionFileDto
import com.Labs2077.civgame.data.repository.rulesets.dto.TerrainDefinitionFileV1Dto
import com.Labs2077.civgame.data.repository.rulesets.dto.toDomainTerrains
import com.labs2077.civilization.core.models.TerrainDefinition
import kotlinx.serialization.json.Json

class TerrainDefinitionGdxFilesRepository(val path: String) : AbstractGdxRepository(),
    TerrainDefinitionRepository {

    override val configuration = Json {
        classDiscriminator = "schemaVersion"
        ignoreUnknownKeys = false
        isLenient = false
    }

    override fun getAll(): List<TerrainDefinition> {
        return when (val result = decode<TerrainDefinitionFileDto>(path)) {
            is TerrainDefinitionFileV1Dto -> result.terrains.toDomainTerrains()
        }
    }
}
