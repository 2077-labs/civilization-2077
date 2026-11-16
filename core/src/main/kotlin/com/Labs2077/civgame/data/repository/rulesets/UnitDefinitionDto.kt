package com.Labs2077.civgame.data.repository.rulesets

import com.labs2077.civilization.core.models.BuildingDefinitionId
import com.labs2077.civilization.core.models.UnitAbility
import com.labs2077.civilization.core.models.UnitDefinition
import com.labs2077.civilization.core.models.UnitDefinitionId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class UnitDefinitionFileDto

@Serializable
@SerialName("1")
internal data class UnitDefinitionFileV1Dto(
    @SerialName("units") val units: List<UnitDefinitionV1Dto>,
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

private fun UnitDefinitionV1Dto.toDomain() = UnitDefinition(
    id = UnitDefinitionId(id),
    displayName = displayName,
    attack = attack,
    attackRange = attackRange,
    canAttackAfterMoving = canAttackAfterMoving,
    defense = defense,
    health = maxHp,
    movement = movement,
    vision = vision,
    abilities = abilities.toDomain(),
    cost = cost.toDomain(),
    upkeep = upkeep.toDomain(),
    requiredBuildingTypeIds = requiredBuildings.toDomainBuildingDefinitionIds(),
    limitPerCivilization = limitPerPlayer,
)

internal fun List<UnitDefinitionV1Dto>.toDomain(): List<UnitDefinition> {
    return map { it.toDomain() }
}

@Serializable
internal enum class UnitAbilityV1Dto {
    ATTACK, FOUND_CITY,
}

private fun UnitAbilityV1Dto.toDomain() = when (this) {
    UnitAbilityV1Dto.ATTACK -> UnitAbility.ATTACK
    UnitAbilityV1Dto.FOUND_CITY -> UnitAbility.FOUND_CITY
}

private fun List<UnitAbilityV1Dto>.toDomain(): Set<UnitAbility> {
    return mapTo(mutableSetOf()) { it.toDomain() }
}

private fun List<String>.toDomainBuildingDefinitionIds() = mapTo(mutableSetOf()) {
    BuildingDefinitionId(it)
}
