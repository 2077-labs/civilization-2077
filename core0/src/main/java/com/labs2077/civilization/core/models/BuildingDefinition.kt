package com.labs2077.civilization.core.models

@JvmInline
value class BuildingDefinitionId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) {
            "Identyfikator definicji budynku nie może być pusty."
        }
    }
}

data class BuildingDefinition(
    val id: BuildingDefinitionId,
    val displayName: String,

    val features: Set<BuildingFeature> = emptySet(),

    val cost: Resources,
    /** Lista rodzajów budynków, które są wymagane do odblokowania budynku danego typu. */
    val requiredBuildingTypes: Set<BuildingDefinition> = emptySet(),
) {
    init {
        require(displayName.isNotBlank()) {
            "Nazwa budynku (displayName) nie może być pusta: $displayName."
        }
        require(!cost.isNegative) {
            "Koszt zakupu budynku (cost) musi być nieujemny: $cost."
        }
    }
}

private val Resources.isNegative: Boolean
    get() = food < 0 || crystals < 0 || metals < 0
