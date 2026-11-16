package com.Labs2077.civgame.data.repository.rulesets

import com.Labs2077.civgame.data.UnitDefinitionRepository
import com.Labs2077.civgame.data.repository.AbstractGdxRepository
import com.labs2077.civilization.core.models.UnitDefinition
import kotlinx.serialization.json.Json

class UnitDefinitionGdxFilesRepository(val path: String) : AbstractGdxRepository(),
    UnitDefinitionRepository {

    override val configuration = Json {
        classDiscriminator = "schemaVersion"
        ignoreUnknownKeys = false
        isLenient = false
    }

    override fun getAll(): List<UnitDefinition> {
        return when (val result = decode<UnitDefinitionFileDto>(path)) {
            is UnitDefinitionFileV1Dto -> result.units.toDomain()
        }
    }
}
