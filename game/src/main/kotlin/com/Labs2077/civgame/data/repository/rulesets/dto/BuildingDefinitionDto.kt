@file:OptIn(ExperimentalSerializationApi::class)

package com.Labs2077.civgame.data.repository.rulesets.dto

import com.labs2077.civilization.core.models.BuildingDefinition
import com.labs2077.civilization.core.models.BuildingDefinitionId
import com.labs2077.civilization.core.models.BuildingFeature
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("schemaVersion")
internal sealed class BuildingDefinitionFileDto

@Serializable
@SerialName("1")
internal data class BuildingDefinitionFileV1Dto(
    val buildings: List<BuildingDefinitionV1Dto>
) : BuildingDefinitionFileDto()

@Serializable
internal data class BuildingDefinitionV1Dto(
    val id: String,
    val displayName: String,
    val features: List<BuildingFeatureV1Dto> = emptyList(),
    val cost: ResourcesV1Dto,
    val requiredBuildings: List<String> = emptyList()
)

private fun BuildingDefinitionV1Dto.toDomainBuildingDefinition() = BuildingDefinition(
    id = BuildingDefinitionId(id),
    displayName = displayName,
    features = features.toDomainBuildingFeatures(),
    cost = cost.toDomainResources(),
    requiredBuildingTypeIds = requiredBuildings.toDomainBuildingDefinitionIds(),
)

internal fun List<BuildingDefinitionV1Dto>.toDomainBuildingDefinitions(): List<BuildingDefinition> {
    return map { it.toDomainBuildingDefinition() }
}

@Serializable
internal enum class BuildingFeatureV1Dto {
    RADAR,
}

private fun BuildingFeatureV1Dto.toDomainBuildingFeature() = when (this) {
    BuildingFeatureV1Dto.RADAR -> BuildingFeature.RADAR
}

private fun List<BuildingFeatureV1Dto>.toDomainBuildingFeatures(): Set<BuildingFeature> {
    return mapTo(mutableSetOf()) { it.toDomainBuildingFeature() }
}

private fun List<String>.toDomainBuildingDefinitionIds(): MutableSet<BuildingDefinitionId> {
    return mapTo(mutableSetOf()) {
        BuildingDefinitionId(it)
    }
}
