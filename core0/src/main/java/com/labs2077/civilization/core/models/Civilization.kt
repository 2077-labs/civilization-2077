package com.labs2077.civilization.core.models

import kotlin.uuid.Uuid

@JvmInline
value class CivilizationId(
    val value: Uuid,
)

class Civilization(
    val id: CivilizationId = CivilizationId(value = Uuid.random()),
)