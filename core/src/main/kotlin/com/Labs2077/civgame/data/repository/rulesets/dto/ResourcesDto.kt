package com.Labs2077.civgame.data.repository.rulesets.dto

import com.labs2077.civilization.core.models.CivilizationResources
import com.labs2077.civilization.core.models.Resources
import kotlinx.serialization.Serializable

@Serializable
internal data class ResourcesV1Dto(
    val metals: Int = 0,
    val food: Int = 0,
    val crystals: Int = 0,
)

internal fun ResourcesV1Dto.toDomainResources() = Resources(
    food = food,
    crystals = crystals,
    metals = metals,
)

@Serializable
internal data class CivilizationResourcesV1Dto(
    val metals: Int = 0,
    val crystals: Int = 0,
)

internal fun CivilizationResourcesV1Dto.toDomainCivilizationResources() = CivilizationResources(
    crystals = crystals,
    metals = metals,
)
