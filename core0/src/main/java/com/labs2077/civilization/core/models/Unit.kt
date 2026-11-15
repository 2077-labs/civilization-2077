package com.labs2077.civilization.core.models

import kotlin.uuid.Uuid

@JvmInline
value class UnitId(
    val value: Uuid,
)

class Unit(
    val id: UnitId = UnitId(value = Uuid.random()),
    val definition: UnitDefinition,
    val civilization: Civilization,
)