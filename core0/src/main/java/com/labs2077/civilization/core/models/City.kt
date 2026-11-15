package com.labs2077.civilization.core.models

import kotlin.uuid.Uuid

@JvmInline
value class CityId(
    val value: Uuid,
)

class City(
    val id: CityId = CityId(value = Uuid.random()),
    val civilization: Civilization,
)