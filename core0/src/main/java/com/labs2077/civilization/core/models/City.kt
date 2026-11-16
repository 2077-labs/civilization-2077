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
    val position: Hexagon,
    resources: CityResources = CityResources(),
    territory: Set<Hexagon> = emptySet(),
    buildings: List<Building> = emptyList(),
    var automaticallyClaimTiles: Boolean = true,
) {
    var name: String = ""
        set(value) {
            require(value.isNotBlank()) {
                "Nazwa miasta nie może być pusta."
            }
            field = value
        }

    var resources: CityResources = CityResources()
        set(value) {
            require(!value.isNegative) {
                "Zasoby miasta muszą być nieujemne: $value."
            }
            field = value
        }

    private val _territory = (territory + position).toMutableSet()
    private val _buildings = mutableMapOf<BuildingId, Building>()

    init {
        this.name = name
        this.resources = resources

        require(isTerritoryConsistent()) {
            "Terytorium miasta musi być spójne: $_territory"
        }

        buildings.forEach(::add)
    }

    val territory: Set<Hexagon> get() = _territory.toSet()
    val buildings: List<Building> get() = _buildings.values.toList()

    fun add(land: Hexagon) {
        require(land !in territory) {
            "Ziemia należy już do terytorium miasta: $land."
        }
        require(checkLandBordersTerritory(land)) {
            "Ziemia musi przylegać do terytorium miasta: $land."
        }
        _territory.add(land)
    }

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

    /** Sprawdza, czy miasto zawiera budynki danego typu. */
    fun hasBuildingOfType(type: BuildingDefinitionId): Boolean {
        return _buildings.values.any { it.definition.id == type }
    }

    /** Sprawdza, czy miasto zawiera budynki posiadające daną funkcjonalność. */
    fun hasBuildingWithFeature(feature: BuildingFeature): Boolean {
        return _buildings.values.any { feature in it.definition.features }
    }

    /** Sprawdza, czy terytorium jest spójne (nie zawiera przerw). */
    private fun isTerritoryConsistent(): Boolean {
        // Terytorium jest niespójne wtedy i tylko wtedy, gdy którykolwiek kawałek ziemi nie sąsiaduje z żadnym innym.
        val adjacentLand = _territory.flatMap { it.neighbors() }
            .toSet()
        return _territory.all { it in adjacentLand }
    }

    /** Sprawdza, czy wskazany kawałek ziemi graniczy z terytorium miasta. */
    private fun checkLandBordersTerritory(land: Hexagon): Boolean {
        return _territory.any { land in it.neighbors() }
    }
}

// TODO: Move to the right command handler.
fun BuildingDefinition.canBeConstructedIn(city: City): Boolean {
    val buildingTypes = city.buildings.map { it.definition }
    return requiredBuildingTypes.all { it in buildingTypes }
}
