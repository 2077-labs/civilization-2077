package com.labs2077.civilization.core.models

class GameState(
    map: TerrainMap,
    players: List<Player> = emptyList(),
    civilizations: List<Civilization> = emptyList(),
) {
    private val _map = map
    private val _players = mutableMapOf<PlayerId, Player>()
    private val _civilizations = mutableMapOf<CivilizationId, Civilization>()

    init {
        // TODO: Test if all entities fit on the map.
        players.forEach(::add)
        civilizations.forEach(::add)
    }

    val map: TerrainMap get() = _map
    val players: List<Player> get() = _players.values.toList()
    val civilizations: List<Civilization> get() = _civilizations.values.toList()

    fun add(player: Player) {
        require(player.id !in _players) {
            "Gracz już istnieje: ${player.id}."
        }
        _players[player.id] = player
    }

    fun add(civilization: Civilization) {
        require(civilization.player.id in _players) {
            "Gracz nie istnieje: ${civilization.player.id}."
        }
        require(civilization.id !in _civilizations) {
            "Cywilizacja już istnieje: ${civilization.id}."
        }
        _civilizations[civilization.id] = civilization
    }
}
