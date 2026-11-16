package com.labs2077.civilization.core.models

import kotlin.uuid.Uuid

@JvmInline
value class CivilizationId(
    val value: Uuid,
)

class Civilization(
    val id: CivilizationId = CivilizationId(value = Uuid.random()),
    val player: Player,
    val color: Color,
    // TODO: Implement stockpile.
    cities: List<City> = emptyList(),
    units: List<Unit> = emptyList(),
) {
    private val _cities = mutableMapOf<CityId, City>()
    private val _units = mutableMapOf<UnitId, Unit>()

    init {
        cities.forEach(::add)
        units.forEach(::add)
    }

    val cities: List<City> get() = _cities.values.toList()
    val units: List<Unit> get() = _units.values.toList()

    /** Cywilizacja upada, gdy straci wszystkie miasta i jednostki. */
    val isAlive: Boolean
        get() = units.isEmpty() && cities.isEmpty()

    /** Koszt utrzymania wszystkich jednostek na turę. */
    val upkeep: Resources
        get() = units.fold(Resources()) { accumulated, unit ->
            accumulated + unit.definition.upkeep
        }

    // TODO: Implement `production` which returns a sum of all generated resources.
    //       This requires cities to have the same kind of mechanism present.

    fun add(city: City) {
        require(city.civilization.id == this.id) {
            "Miasto należy do innej cywilizacji: ${city.civilization.id}."
        }
        require(city.id !in _cities) {
            "Miasto należy już do cywilizacji: ${city.id}."
        }
        _cities[city.id] = city
    }

    fun add(unit: Unit) {
        require(unit.civilization.id == this.id) {
            "Jednostka należy do innej cywilizacji: ${unit.civilization.id}."
        }
        require(unit.id !in _units) {
            "Jednostka należy już do cywilizacji: ${unit.id}."
        }
        _units[unit.id] = unit
    }

    fun remove(cityId: CityId) {
        _cities.remove(cityId)
    }

    fun remove(unitId: UnitId) {
        _units.remove(unitId)
    }
}
