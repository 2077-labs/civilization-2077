@file:OptIn(ExperimentalSerializationApi::class)

package com.Labs2077.civgame.data.repository.rulesets.dto

import com.Labs2077.civgame.client.decoration.DecorationId
import com.Labs2077.civgame.client.decoration.DecorationRule
import com.Labs2077.civgame.client.decoration.TerrainDecorations
import com.labs2077.civilization.core.models.TerrainDefinitionId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("schemaVersion")
internal sealed class DecorationDefinitionFileDto

@Serializable
@SerialName("1")
internal data class DecorationDefinitionFileV1Dto(
    val terrains: List<TerrainDecorationsV1Dto>
) : DecorationDefinitionFileDto()

@Serializable
internal data class TerrainDecorationsV1Dto(
    val terrainId: String,
    val chance: Float = 1f,
    val minCount: Int = 1,
    val maxCount: Int,
    val rules: List<DecorationRuleV1Dto>
)

@Serializable
internal data class DecorationRuleV1Dto(
    val decorationId: String,
    val weight: Int,
    val maxPerTile: Int = 1
)

private fun TerrainDecorationsV1Dto.toDomain() = TerrainDecorations(
    terrainId = TerrainDefinitionId(terrainId),
    chance = chance,
    minCount = minCount,
    maxCount = maxCount,
    rules = rules.map { DecorationRule(DecorationId(it.decorationId), it.weight, it.maxPerTile) }
)

internal fun List<TerrainDecorationsV1Dto>.toDomainTerrainDecorations(): List<TerrainDecorations> =
    map { it.toDomain() }
