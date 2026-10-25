package com.Labs2077.civgame.engine.resources

import kotlinx.serialization.Serializable

/** Ilości zasobów; wspólny typ dla kosztu rekrutacji i utrzymania. */
@Serializable
data class ResourceBundle(
    val metals: Int = 0,
    val food: Int = 0,
    val crystals: Int = 0
) {
    fun isNonNegative(): Boolean = metals >= 0 && food >= 0 && crystals >= 0
    val isEmpty: Boolean get() = metals == 0 && food == 0 && crystals == 0
}
