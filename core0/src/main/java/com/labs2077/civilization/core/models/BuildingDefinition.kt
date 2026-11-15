package com.labs2077.civilization.core.models

@JvmInline
value class BuildingDefinitionId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Identyfikator definicji terenu nie może być pusty." }
    }
}

data class BuildingDefinition(
    val id: BuildingDefinitionId,
)