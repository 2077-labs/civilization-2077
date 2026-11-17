@file:OptIn(ExperimentalSerializationApi::class)

package com.Labs2077.civgame.data.repository.rulesets.dto

import com.labs2077.civilization.core.models.BuildingDefinitionId
import com.labs2077.civilization.core.models.BuildingProductionModifier
import com.labs2077.civilization.core.models.ProductionEffectMode
import com.labs2077.civilization.core.models.TerrainDefinition
import com.labs2077.civilization.core.models.TerrainDefinitionId
import com.labs2077.civilization.core.models.TerrainProduction
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
internal sealed class TerrainDefinitionFileDto

@Serializable
@SerialName("1")
internal data class TerrainDefinitionFileV1Dto(
    @SerialName("terrains") val terrains: List<TerrainDefinitionV1Dto>
) : TerrainDefinitionFileDto()

@Serializable
internal data class TerrainDefinitionV1Dto(
    val id: String,
    val displayName: String,
    val passable: Boolean,
    val movementCost: Double,
    val production: TerrainProductionV1Dto,
)

private fun TerrainDefinitionV1Dto.toDomain() = TerrainDefinition(
    id = TerrainDefinitionId(id),
    displayName = displayName,
    passable = passable,
    movementCost = movementCost,
    production = production.toDomain(),
)

internal fun List<TerrainDefinitionV1Dto>.toDomainTerrains(): List<TerrainDefinition> {
    return map { it.toDomain() }
}

@Serializable
@JsonClassDiscriminator("type")
internal sealed interface TerrainProductionV1Dto {

    @Serializable
    @SerialName("fixed")
    data class Fixed(
        val resources: ResourcesV1Dto,
    ) : TerrainProductionV1Dto

    @Serializable
    @SerialName("building_modifiers")
    data class BuildingModifiers(
        val baseResources: ResourcesV1Dto,
        val modifiers: List<BuildingProductionModifierV1Dto>,
    ) : TerrainProductionV1Dto
}

private fun TerrainProductionV1Dto.Fixed.toDomain() = TerrainProduction.Fixed(
    resources = resources.toDomain(),
)

private fun TerrainProductionV1Dto.BuildingModifiers.toDomain() =
    TerrainProduction.BuildingModifiers(
        baseResources = baseResources.toDomain(),
        modifiers = modifiers.toDomainModifiers(),
    )

private fun TerrainProductionV1Dto.toDomain() = when (this) {
    is TerrainProductionV1Dto.Fixed -> toDomain()
    is TerrainProductionV1Dto.BuildingModifiers -> toDomain()
}

@Serializable
internal data class BuildingProductionModifierV1Dto(
    val buildingId: String,
    val mode: ProductionEffectModeV1Dto,
    val resources: ResourcesV1Dto,
    val priority: Int,
)

private fun BuildingProductionModifierV1Dto.toDomain() = BuildingProductionModifier(
    buildingTypeId = BuildingDefinitionId(buildingId),
    mode = mode.toDomain(),
    resources = resources.toDomain(),
    priority = priority,
)

private fun List<BuildingProductionModifierV1Dto>.toDomainModifiers(): List<BuildingProductionModifier> {
    return map { it.toDomain() }
}

@Serializable
internal enum class ProductionEffectModeV1Dto {
    ADD, REPLACE,
}

private fun ProductionEffectModeV1Dto.toDomain() = when (this) {
    ProductionEffectModeV1Dto.ADD -> ProductionEffectMode.ADD
    ProductionEffectModeV1Dto.REPLACE -> ProductionEffectMode.REPLACE
}
