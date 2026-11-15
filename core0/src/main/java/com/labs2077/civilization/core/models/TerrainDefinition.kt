package com.labs2077.civilization.core.models

@JvmInline
value class TerrainDefinitionId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Identyfikator definicji terenu nie może być pusty." }
    }
}

data class TerrainDefinition(
    val id: TerrainDefinitionId,
)