package com.Labs2077.civgame.data.repository.rulesets

import com.Labs2077.civgame.client.decoration.TerrainDecorations
import com.Labs2077.civgame.data.DecorationDefinitionRepository
import com.Labs2077.civgame.data.repository.AbstractGdxRepository
import com.Labs2077.civgame.data.repository.rulesets.dto.DecorationDefinitionFileDto
import com.Labs2077.civgame.data.repository.rulesets.dto.DecorationDefinitionFileV1Dto
import com.Labs2077.civgame.data.repository.rulesets.dto.toDomainTerrainDecorations

class DecorationDefinitionGdxFilesRepository(val path: String) : AbstractGdxRepository(),
    DecorationDefinitionRepository {
    override fun getAll(): List<TerrainDecorations> {
        return when (val result = decode<DecorationDefinitionFileDto>(path)) {
            is DecorationDefinitionFileV1Dto -> result.terrains.toDomainTerrainDecorations()
        }
    }
}
