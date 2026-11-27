package com.labs2077.civilization.core.map_generation

import com.labs2077.civilization.core.models.HexagonGrid
import com.labs2077.civilization.core.models.Terrain
import com.labs2077.civilization.core.models.TerrainDefinition
import com.labs2077.civilization.core.models.TerrainMap
import com.labs2077.civilization.core.models.TerrainRuleset
import kotlin.random.Random

/**
 * Generator tworzący skupiska biomów wokół losowych punktów centralnych (ognisk).
 *
 * Algorytm działa w dwóch etapach:
 * 1. Dla każdego rodzaju terenu z [TerrainRuleset] losuje zestaw punktów centralnych na mapie
 *    (większą liczbę dla pustyni, aby pełniła rolę dominującego terenu bazowego).
 * 2. Każdemu kafelkowi przypisuje definicję terenu najbliższego ogniska w metryce
 *    heksagonalnej ([Hexagon.distanceTo]), powiększonej o losowy szum odległości,
 *    dzięki czemu granice między biomami są nieregularne.
 */
class ClusterMapGenerator : MapGenerator {

    private companion object {
        /** Identyfikator dominującego terenu bazowego otrzymującego więcej ognisk. */
        const val DOMINANT_TERRAIN_ID = "desert"

        /** Liczba ognisk losowanych dla terenu dominującego. */
        const val DOMINANT_CLUSTER_COUNT = 4

        /** Domyślna liczba ognisk losowanych dla pozostałych rodzajów terenu. */
        const val DEFAULT_CLUSTER_COUNT = 2

        /** Maksymalne losowe zaburzenie odległości (w heksach) rozmywające granice biomów. */
        const val DISTANCE_NOISE_AMPLITUDE = 1.5f
    }

    override fun generate(
        config: MapGenerationConfig,
        terrains: TerrainRuleset,
    ): TerrainMap {
        val rng = Random(config.seed)
        // Najpierw dla każdego terenu losujemy kilka punktów centralnych, wokół których będą się
        // tworzyć skupiska danego terenu.
        val centers = terrains.definitions.values.flatMap { terrain ->
            List(clusterCountFor(terrain)) {
                val pos =
                    HexagonGrid.coordinate(rng.nextInt(config.width), rng.nextInt(config.height))
                terrain to pos
            }
        }

        val tiles = List(config.width * config.height) { i ->
            val pos = HexagonGrid.coordinate(i % config.width, i / config.width)
            // Każdemu polu przypisujemy teren ogniska o najmniejszej zaburzonej odległości.
            val definition =
                centers.minBy { (_, c) -> c.distanceTo(pos) + rng.nextFloat() * DISTANCE_NOISE_AMPLITUDE }.first
            Terrain(definition = definition, position = pos)
        }
        return TerrainMap(
            HexagonGrid(config.width, config.height, tiles)
        )
    }

    /**
     * Określa liczbę ognisk losowanych dla danej definicji terenu.
     */
    private fun clusterCountFor(terrain: TerrainDefinition): Int {
        return if (terrain.id.value == DOMINANT_TERRAIN_ID) {
            DOMINANT_CLUSTER_COUNT
        } else {
            DEFAULT_CLUSTER_COUNT
        }
    }
}
