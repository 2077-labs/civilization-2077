package com.Labs2077.civgame.data.repository.rulesets

import com.labs2077.civilization.core.models.CivilizationResources
import com.labs2077.civilization.core.models.Resources
import kotlinx.serialization.Serializable

@Serializable
internal data class ResourcesV1Dto(
    val metals: Int,
    val food: Int,
    val crystals: Int,
)

internal fun ResourcesV1Dto.toDomain() = Resources(
    food = food,
    crystals = crystals,
    metals = metals,
)

@Serializable
internal data class CivilizationResourcesV1Dto(
    val metals: Int,
    val crystals: Int,
)

internal fun CivilizationResourcesV1Dto.toDomain() = CivilizationResources(
    crystals = crystals,
    metals = metals,
)
