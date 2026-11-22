package com.labs2077.civilization.core.map_generation

import com.labs2077.civilization.core.models.HexagonGrid
import com.labs2077.civilization.core.models.Terrain
import com.labs2077.civilization.core.models.TerrainRuleset

/** Parametry tworzenia nowej mapy (docelowo przekazywane z menu nowej gry). */
data class MapGenerationConfig(
    val width: Int,
    val height: Int,
    val seed: Long,
) {
    init {
        require(width > 0) { "Szerokość mapy musi być dodatnia: $width." }
        require(height > 0) { "Wysokość mapy musi być dodatnia: $height." }
    }
}

/**
 * Interfejs generatora mapy. Umożliwia pozyskanie mapy w postaci heksagonalnej siatki kafelków
 * terenu na podstawie konfiguracji i zestawu reguł terenów.
 */
fun interface MapGenerator {
    fun generate(config: MapGenerationConfig, terrains: TerrainRuleset): HexagonGrid<Terrain>
}
