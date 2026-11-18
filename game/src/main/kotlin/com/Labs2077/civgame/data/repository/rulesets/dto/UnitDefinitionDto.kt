@file:OptIn(ExperimentalSerializationApi::class)

package com.Labs2077.civgame.data.repository.rulesets.dto

import com.labs2077.civilization.core.models.BuildingDefinitionId
import com.labs2077.civilization.core.models.UnitAbility
import com.labs2077.civilization.core.models.UnitDefinition
import com.labs2077.civilization.core.models.UnitDefinitionId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("schemaVersion")
internal sealed class UnitDefinitionFileDto

@Serializable
@SerialName("1")
internal data class UnitDefinitionFileV1Dto(
    val units: List<UnitDefinitionV1Dto>,
) : UnitDefinitionFileDto()

@Serializable
internal data class UnitDefinitionV1Dto(
    val id: String,
    val displayName: String,
    val attack: Int,
    val attackRange: Int,
    val canAttackAfterMoving: Boolean = true,
    val defense: Int,
    val maxHp: Int,
    val movement: Int,
    val vision: Int,
    val abilities: List<UnitAbilityV1Dto>,
    val cost: ResourcesV1Dto,
    val upkeep: CivilizationResourcesV1Dto,
    val requiredBuildings: List<String>,
    val limitPerPlayer: Int? = null,
)

private fun UnitDefinitionV1Dto.toDomainUnitDefinition() = UnitDefinition(
    id = UnitDefinitionId(id),
    displayName = displayName,
    attack = attack,
    attackRange = attackRange,
    canAttackAfterMoving = canAttackAfterMoving,
    defense = defense,
    health = maxHp,
    movement = movement,
    vision = vision,
    abilities = abilities.toDomainUnitAbilities(),
    cost = cost.toDomainResources(),
    upkeep = upkeep.toDomainCivilizationResources(),
    requiredBuildingTypeIds = requiredBuildings.toDomainBuildingDefinitionIds(),
    limitPerCivilization = limitPerPlayer,
)

internal fun List<UnitDefinitionV1Dto>.toDomainUnitDefinitions(): List<UnitDefinition> {
    return map { it.toDomainUnitDefinition() }
}

@Serializable
internal enum class UnitAbilityV1Dto {
    ATTACK, FOUND_CITY,
}

private fun UnitAbilityV1Dto.toDomainUnitAbility() = when (this) {
    UnitAbilityV1Dto.ATTACK -> UnitAbility.ATTACK
    UnitAbilityV1Dto.FOUND_CITY -> UnitAbility.FOUND_CITY
}

private fun List<UnitAbilityV1Dto>.toDomainUnitAbilities(): Set<UnitAbility> {
    return mapTo(mutableSetOf()) { it.toDomainUnitAbility() }
}

private fun List<String>.toDomainBuildingDefinitionIds(): MutableSet<BuildingDefinitionId> {
    return mapTo(mutableSetOf()) {
        BuildingDefinitionId(it)
    }
}
