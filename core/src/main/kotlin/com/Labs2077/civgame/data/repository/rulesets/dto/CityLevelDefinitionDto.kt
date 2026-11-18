@file:OptIn(ExperimentalSerializationApi::class)

package com.Labs2077.civgame.data.repository.rulesets.dto

import com.labs2077.civilization.core.models.CityLevelDefinition
import com.labs2077.civilization.core.models.CityLevelDefinitionId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("schemaVersion")
internal sealed class CityLevelDefinitionFileDto

@Serializable
@SerialName("1")
internal data class CityLevelDefinitionFileV1Dto(
    val levels: List<CityLevelDefinitionV1Dto>,
) : CityLevelDefinitionFileDto()

@Serializable
internal data class CityLevelDefinitionV1Dto(
    val id: String,
    val level: Int,
    val centerProduction: ResourcesV1Dto,
    val levelUpRequirement: CityResourcesV1Dto? = null,
    val radius: Int,
    val maxTiles: Int,
)

private fun CityLevelDefinitionV1Dto.toDomainCityLevelDefinition() = CityLevelDefinition(
    id = CityLevelDefinitionId(id),
    level = level,
    centerProduction = centerProduction.toDomainResources(),
    levelUpRequirement = levelUpRequirement?.toDomainCityResources(),
    maximumCityRadius = radius,
    maximumTerritorySize = maxTiles,
)

internal fun List<CityLevelDefinitionV1Dto>.toDomainCityLevelDefinitions(): List<CityLevelDefinition> {
    return map { it.toDomainCityLevelDefinition() }
}
