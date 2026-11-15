package com.labs2077.civilization.core.models

@JvmInline
value class UnitDefinitionId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Identyfikator definicji jednostki nie może być pusty." }
    }
}

data class UnitDefinition(
    val id: UnitDefinitionId,
)