package com.labs2077.civilization.core.models

import kotlin.uuid.Uuid

@JvmInline
value class BuildingId(
    val value: Uuid,
)

class Building(
    val id: BuildingId = BuildingId(value = Uuid.random()),
    val definition: BuildingDefinition,
    val city: City,
)
