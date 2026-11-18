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
    level: CityLevelDefinition,
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

    var level: CityLevelDefinition = level
        set(value) {
            require(value.level >= field.level) {
                "Miasto nie może awansować na niższy poziom: ${value.level} (obecnie ${field.level})."
            }
            require(value.level <= field.level + 1) {
                "Miasto może awansować jedynie na poziom o 1 większy: ${value.level} (obecnie ${field.level})."
            }
            require(canLevelUp()) {
                "Miasto nie posiada wymaganej ilości zasobów do awansu na kolejny poziom: ${field.levelUpRequirement} (obecnie ${resources})."
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

        // TODO: Check if city territory is consistent (does not have gaps).
        buildings.forEach(::add)
    }

    val territory: Set<Hexagon> get() = _territory.toSet()
    val buildings: List<Building> get() = _buildings.values.toList()

    fun add(land: Hexagon) {
        require(land !in territory) {
            "Ziemia należy już do terytorium miasta: $land."
        }
        require(territory.size < level.maximumTerritorySize) {
            "Miasto posiada już maksymalną ilość ziemi dla swojego poziomu."
        }
        require(land.isWithinAllowedDistanceFromCityCenter()) {
            "Odległość ziemi od środka miasta nie może przekraczać jego promienia: ${level.maximumCityRadius}."
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

    /** Sprawdza, czy możliwy jest awans na kolejny poziom miasta na postawie zgromadzonej żywności. */
    fun canLevelUp(): Boolean {
        return (level.levelUpRequirement ?: return false) < resources
    }

    /** Pobiera typy budynków w mieście. */
    fun getBuildingTypes(): Set<BuildingDefinitionId> {
        return _buildings.values.mapTo(mutableSetOf()) { it.definition.id }
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
        return _territory.any { land in it.neighbors() }
    }

    /** Oblicza terytorium, które może zostać przez miasto zajęte.
     * Nie sprawdza natomiast, czy ziemia jest już zajęta przez innego gracza, lub czy jej przejęcie jest w ogóle możliwe. */
    private fun getClaimableLand(): Set<Hexagon> {
        val claimable = mutableSetOf<Hexagon>()

        if (territory.size >= level.maximumTerritorySize) {
            return claimable
        }

        // Dostępne terytorium może zawierać jedynie ziemię bezpośrednio przyległą do obszaru zajętego przez miasto.
        _territory.forEach { claimable.addAll(it.neighbors()) }
        // Dostępne terytorium nie może zawierać obszaru zajętego już przez miasto.
        _territory.forEach { claimable.remove(it) }

        claimable.removeIf { !it.isWithinAllowedDistanceFromCityCenter() }

        return claimable
    }

    private fun Hexagon.isWithinAllowedDistanceFromCityCenter(): Boolean {
        return distanceTo(position) <= level.maximumCityRadius
    }
}

// TODO: Move to the right command handler.
fun BuildingDefinition.canBeConstructedIn(city: City): Boolean {
    val buildingTypeIds = city.getBuildingTypes()
    return requiredBuildingTypeIds.all { it in buildingTypeIds }
}
