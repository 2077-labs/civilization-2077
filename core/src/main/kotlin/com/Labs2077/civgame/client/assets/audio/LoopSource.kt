package com.Labs2077.civgame.client.assets.audio

import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.UnitId

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
        val unitId: UnitId,
    ) : LoopSource

    /**
     * Konkretne pole mapy.
     */
    data class Tile(
        val position: Hexagon
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
