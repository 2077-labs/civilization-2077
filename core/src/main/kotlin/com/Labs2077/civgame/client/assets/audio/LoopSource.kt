package com.Labs2077.civgame.client.assets.audio

import com.Labs2077.civgame.engine.map.HexCoordinate

/**
 * Identyfikuje obiekt lub obszar będący źródłem zapętlonego dźwięku.
 *
 * Identyfikatory są rozróżniane według rodzaju źródła.
 */
sealed interface LoopSource {

    /**
     * Konkretna instancja jednostki.
     */
    data class Unit(
        val instanceId: Long
    ) : LoopSource

    /**
     * Konkretne pole mapy.
     */
    data class Tile(
        val position: HexCoordinate
    ) : LoopSource

    /**
     * Dźwięk związany z całym światem, np. pogodą.
     */
    data object World : LoopSource

    /**
     * Konkretna warstwa ambientu lub obszar środowiskowy.
     */
    data class Ambient(
        val id: String
    ) : LoopSource
}
