package com.Labs2077.civgame.data.repository.rulesets.dto

import com.labs2077.civilization.core.models.BuildingDefinition
import com.labs2077.civilization.core.models.BuildingDefinitionId
import com.labs2077.civilization.core.models.BuildingFeature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
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

private fun BuildingDefinitionV1Dto.toDomain() = BuildingDefinition(
    id = BuildingDefinitionId(id),
    displayName = displayName,
    features = features.toDomain(),
    cost = cost.toDomain(),
    requiredBuildingTypeIds = requiredBuildings.toDomainBuildingDefinitionIds(),
)

internal fun List<BuildingDefinitionV1Dto>.toDomain(): List<BuildingDefinition> {
    return map { it.toDomain() }
}

@Serializable
internal enum class BuildingFeatureV1Dto {
    RADAR,
}

private fun BuildingFeatureV1Dto.toDomain() = when (this) {
    BuildingFeatureV1Dto.RADAR -> BuildingFeature.RADAR
}

private fun List<BuildingFeatureV1Dto>.toDomain(): Set<BuildingFeature> {
    return mapTo(mutableSetOf()) { it.toDomain() }
}

private fun List<String>.toDomainBuildingDefinitionIds(): MutableSet<BuildingDefinitionId> {
    return mapTo(mutableSetOf()) {
        BuildingDefinitionId(it)
    }
}
