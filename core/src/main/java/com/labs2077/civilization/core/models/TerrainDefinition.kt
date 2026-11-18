package com.labs2077.civilization.core.models

@JvmInline
value class TerrainDefinitionId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) {
            "Identyfikator definicji terenu nie może być pusty."
        }
    }
}

data class TerrainDefinition(
    val id: TerrainDefinitionId,
    val displayName: String,

    val passable: Boolean,
    val movementCost: Double,

    val production: TerrainProduction,
) {
    init {
        require(displayName.isNotBlank()) {
            "Nazwa terenu (displayName) nie może być pusta: $displayName."
        }
        require(movementCost > 0) {
            "Koszt ruchu (movementCost) musi być dodatni: $movementCost."
        }
    }
}
