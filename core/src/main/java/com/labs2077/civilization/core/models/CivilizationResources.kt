package com.labs2077.civilization.core.models

data class CivilizationResources(
    val crystals: Int = 0,
    val metals: Int = 0,
) {
    operator fun plus(other: CivilizationResources) = CivilizationResources(
        crystals = crystals + other.crystals,
        metals = metals + other.metals,
    )

    val isNegative: Boolean
        get() = crystals < 0 || metals < 0
}
