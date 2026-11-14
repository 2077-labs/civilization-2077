package com.Labs2077.civgame.engine.buildings

import kotlinx.serialization.Serializable

/**
 * Funkcjonalnosć budynku, dzięki temu silnik może pytać bezpośrednio o funkcjonalności budynku, zamiast sprawdzać jego identyfikator.
 */
@Serializable
enum class BuildingFeature {
    RADAR
}
