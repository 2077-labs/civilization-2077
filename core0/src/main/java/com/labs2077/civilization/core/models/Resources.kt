package com.labs2077.civilization.core.models

data class Resources(
    val food: Int = 0,
    val crystals: Int = 0,
    val metals: Int = 0,
) {
    operator fun plus(other: Resources) = Resources(
        food = food + other.food,
        crystals = crystals + other.crystals,
        metals = metals + other.metals,
    )

    val isNegative: Boolean
        get() = food < 0 || crystals < 0 || metals < 0
}
