package com.labs2077.civilization.core.models

class TerrainMap(
    tiles: HexagonGrid<Terrain>,
) {
    private val _tiles = tiles.toMutableHexagonGrid()

    // Ze względu na wydajność, zamiast dynamicznego gettera,
    // pole będzie aktualizowane wyłącznie z wewnątrz klasy.
    var tiles: HexagonGrid<Terrain> = _tiles.toHexagonGrid()
        private set

    companion object
}

fun TerrainMap.Companion.random(width: Int, height: Int, ruleset: TerrainRuleset): TerrainMap {
    val options = ruleset.definitions.values

    val tiles = HexagonGrid.create(width, height) { position ->
        val definition = options.random()
        Terrain(definition, position)
    }

    return TerrainMap(tiles)
}
