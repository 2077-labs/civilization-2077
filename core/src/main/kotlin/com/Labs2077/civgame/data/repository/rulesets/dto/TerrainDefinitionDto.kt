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
@JsonClassDiscriminator("schemaVersion")
internal sealed class TerrainDefinitionFileDto

@Serializable
@SerialName("1")
internal data class TerrainDefinitionFileV1Dto(
    val terrains: List<TerrainDefinitionV1Dto>
) : TerrainDefinitionFileDto()

@Serializable
internal data class TerrainDefinitionV1Dto(
    val id: String,
    val displayName: String,
    val passable: Boolean,
    val movementCost: Double,
    val production: TerrainProductionV1Dto,
)

private fun TerrainDefinitionV1Dto.toDomainTerrainDefinition() = TerrainDefinition(
    id = TerrainDefinitionId(id),
    displayName = displayName,
    passable = passable,
    movementCost = movementCost,
    production = production.toDomainTerrainProduction(),
)

internal fun List<TerrainDefinitionV1Dto>.toDomainTerrainDefinitions(): List<TerrainDefinition> {
    return map { it.toDomainTerrainDefinition() }
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

private fun TerrainProductionV1Dto.Fixed.toDomainFixedTerrainProduction() = TerrainProduction.Fixed(
    resources = resources.toDomainResources(),
)

private fun TerrainProductionV1Dto.BuildingModifiers.toDomainBuildingModifiersTerrainProduction() =
    TerrainProduction.BuildingModifiers(
        baseResources = baseResources.toDomainResources(),
        modifiers = modifiers.toDomainBuildingProductionModifiers(),
    )

private fun TerrainProductionV1Dto.toDomainTerrainProduction() = when (this) {
    is TerrainProductionV1Dto.Fixed -> toDomainFixedTerrainProduction()
    is TerrainProductionV1Dto.BuildingModifiers -> toDomainBuildingModifiersTerrainProduction()
}

@Serializable
internal data class BuildingProductionModifierV1Dto(
    val buildingId: String,
    val mode: ProductionEffectModeV1Dto,
    val resources: ResourcesV1Dto,
    val priority: Int,
)

private fun BuildingProductionModifierV1Dto.toDomainBuildingProductionModifier() =
    BuildingProductionModifier(
        buildingTypeId = BuildingDefinitionId(buildingId),
        mode = mode.toDomainProductionEffectMode(),
        resources = resources.toDomainResources(),
        priority = priority,
    )

private fun List<BuildingProductionModifierV1Dto>.toDomainBuildingProductionModifiers(): List<BuildingProductionModifier> {
    return map { it.toDomainBuildingProductionModifier() }
}

@Serializable
internal enum class ProductionEffectModeV1Dto {
    ADD, REPLACE,
}

private fun ProductionEffectModeV1Dto.toDomainProductionEffectMode() = when (this) {
    ProductionEffectModeV1Dto.ADD -> ProductionEffectMode.ADD
    ProductionEffectModeV1Dto.REPLACE -> ProductionEffectMode.REPLACE
}
