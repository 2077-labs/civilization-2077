package com.labs2077.civilization.core.models

import kotlin.uuid.Uuid

@JvmInline
value class CityId(
    val value: Uuid,
)

class City(
    val id: CityId = CityId(value = Uuid.random()),
    val civilization: Civilization,
    name: String,
    var position: Hexagon,
    // TODO: Implement stockpile.
    buildings: List<Building> = emptyList(),
    territory: Set<Hexagon> = emptySet(),
    var automaticallyClaimTiles: Boolean = true,
) {
    var name: String = ""
        set(value) {
            require(value.isNotBlank()) {
                "Nazwa miasta nie może być pusta."
            }
            field = value
        }

    init {
        this.name = name
    }

    private val _buildings = mutableMapOf<BuildingId, Building>()
    private val _territory = (territory + position).toMutableSet()

    init {
        buildings.forEach(::add)
        // TODO: Add territory validation (fields should be their neighbors).
    }

    val buildings: List<Building> = _buildings.values.toList()
    val territory: Set<Hexagon> = _territory.toSet()

    fun add(building: Building) {
        require(building.city.id == this.id) {
            "Budynek należy do innego miasta: ${building.city.id}."
        }
        require(building.id !in _buildings) {
            "Budynek należy już do miasta: ${building.id}."
        }
        require(!hasBuildingOfType(building.definition.id)) {
            "Budynek tego typu należy już do miasta: ${building.definition.id}."
        }
        _buildings[building.id] = building
    }

    fun add(land: Hexagon) {
        require(land !in territory) {
            "Ziemia należy już do terytorium miasta: $land."
        }
        require(checkLandBordersTerritory(land)) {
            "Ziemia musi przylegać do terytorium miasta: $land."
        }
        _territory.add(land)
    }

    fun remove(buildingId: BuildingId) {
        _buildings.remove(buildingId)
    }

    /** Sprawdza, czy miasto zawiera budynki danego typu. */
    fun hasBuildingOfType(type: BuildingDefinitionId): Boolean {
        return _buildings.values.any { it.definition.id == type }
    }

    /** Sprawdza, czy miasto zawiera budynki posiadające daną funkcjonalność. */
    fun hasBuildingWithFeature(feature: BuildingFeature): Boolean {
        return _buildings.values.any { feature in it.definition.features }
    }

    /** Sprawdza, czy wskazany kawałek ziemi graniczy z terytorium miasta. */
    private fun checkLandBordersTerritory(land: Hexagon): Boolean {
        return territory.any { land in it.neighbors() }
    }
}

// TODO: Move to the right command handler.
fun BuildingDefinition.canBeConstructedIn(city: City): Boolean {
    val buildingTypes = city.buildings.map { it.definition }
    return requiredBuildingTypes.all { it in buildingTypes }
}
