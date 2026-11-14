package com.Labs2077.civgame.engine.player

import com.Labs2077.civgame.engine.PlayerSetup
import com.Labs2077.civgame.engine.city.City
import com.Labs2077.civgame.engine.map.HexCoordinate
import com.Labs2077.civgame.engine.resources.ResourceBundle
import com.Labs2077.civgame.engine.rules.UnitId
import com.Labs2077.civgame.engine.rules.units.UnitRuleset
import com.Labs2077.civgame.engine.units.UnitInstance

data class Player(
    val id: Int,
    val name: String,
    val colorRgb: Int,
    val isComputer: Boolean,
    val units: List<UnitInstance> = emptyList(),
    val cities: List<City> = emptyList(),
    // Istotne jest to, że stockpile zawiera jedynie metale i kryształy, a żywność jest przechowywana w pojedynczych miastach
    val stockpile: ResourceBundle = ResourceBundle(),
    val exploredTiles: Set<HexCoordinate> = emptySet()
) {
    init {
        require(id >= 0) { "id gracza nie może być ujemne" }
        require(name.isNotBlank()) { "nazwa gracza nie może być pusta" }
        require(stockpile.isNonNegative()) { "stockpile nie może być ujemny" }
        require(stockpile.food == 0) { "żywność jest przechowywana w miastach, stockpile.food musi być 0" }
        require(units.all { it.ownerId == id }) { "Jednostka innego gracza na liście jednostek gracza $id" }
        require(cities.all { it.ownerId == id }) { "Miasto innego gracza na liście miast gracza $id" }
        require(units.map { it.instanceId }.distinct().size == units.size) { "Powtórzone instanceId jednostek" }
        require(cities.map { it.id }.distinct().size == cities.size) { "Powtórzone id miast" }
    }

    /** Gracz odpada, gdy traci wszystkie miasta i jednostki */
    val isAlive: Boolean get() = units.isNotEmpty() || cities.isNotEmpty()

    fun unit(instanceId: Long): UnitInstance? = units.firstOrNull { it.instanceId == instanceId }
    fun city(cityId: Long): City? = cities.firstOrNull { it.id == cityId }
    fun cityAt(position: HexCoordinate): City? = cities.firstOrNull { it.position == position }

    /** Liczba jednostek danego typu (do sprawdzania limitPerPlayer). */
    fun unitCount(definitionId: UnitId): Int = units.count { it.definitionId == definitionId }

    /** Łączne utrzymanie wszystkich jednostek na turę. */
    fun totalUpkeep(unitRuleset: UnitRuleset): ResourceBundle {
        var current = ResourceBundle()
        units.forEach { unit ->
            val definition = unitRuleset.get(unit.definitionId)
            current += definition.upkeep
        }
        return current
    }

    // zasoby

    fun withResources(delta: ResourceBundle): Player = copy(stockpile = stockpile + delta)

    // jednostki

    fun withUnit(unit: UnitInstance): Player = copy(units = units + unit)

    fun withoutUnit(instanceId: Long): Player = copy(units = units.filter { it.instanceId != instanceId })

    fun updateUnit(updated: UnitInstance): Player {
        require(units.any { it.instanceId == updated.instanceId }) { "Nie ma jednostki o instanceId=${updated.instanceId}" }
        return copy(units = units.map { if (it.instanceId == updated.instanceId) updated else it })
    }

    // miasta

    fun withCity(city: City): Player = copy(cities = cities + city)

    fun withoutCity(cityId: Long): Player = copy(cities = cities.filter { it.id != cityId })

    fun updateCity(updated: City): Player {
        require(cities.any { it.id == updated.id }) { "Nie ma miasta o id=${updated.id}" }
        return copy(cities = cities.map { if (it.id == updated.id) updated else it })
    }

    // mgła wojny

    fun withExplored(tiles: Collection<HexCoordinate>): Player = copy(exploredTiles = exploredTiles + tiles)

    companion object {
        fun fromSetup(setup: PlayerSetup, isComputer: Boolean = false): Player =
            Player(
                id = setup.id,
                name = setup.name,
                colorRgb = setup.colorRgb,
                isComputer = isComputer
            )
    }
}
