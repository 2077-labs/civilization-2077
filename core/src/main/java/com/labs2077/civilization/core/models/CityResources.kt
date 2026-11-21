package com.labs2077.civilization.core.models

data class CityResources(
    val food: Int = 0,
) {
    operator fun plus(other: CityResources) = CityResources(
        food = food + other.food,
    )

    operator fun compareTo(other: CityResources): Int {
        return food - other.food
    }

    val isNegative: Boolean
        get() = food < 0
}
