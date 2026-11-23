package com.labs2077.civilization.engine

import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.CivilizationId
import com.labs2077.civilization.core.models.Player
import com.labs2077.civilization.core.models.PlayerId

internal class State(
    players: List<Player> = emptyList(),
    civilizations: List<Civilization> = emptyList(),
) {
    private val _players = mutableMapOf<PlayerId, Player>()
    private val _civilizations = mutableMapOf<CivilizationId, Civilization>()

    init {
        players.forEach(::add)
        civilizations.forEach(::add)
    }

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
