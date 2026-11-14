package com.Labs2077.civgame.engine.city

import com.Labs2077.civgame.engine.buildings.BuildingDefinition
import com.Labs2077.civgame.engine.buildings.BuildingFeature
import com.Labs2077.civgame.engine.map.HexCoordinate
import com.Labs2077.civgame.engine.rules.buildings.BuildingRuleset
import com.Labs2077.civgame.engine.rules.units.BuildingId

/**
 * Stan konkretnego miasta.
 *
 * @property id unikalny identyfikator miasta w grze
 * @property name nazwa miasta
 * @property ownerId id gracza
 * @property position pozycja miasta na mapie
 * @property buildings zbudowane budynki w mieście (max 1 z każdego typu)
 * @property ownedTiles pola należące do miasta, BEZ pola centrum (limit maxTiles potem dotyczy tej listy)
 * @property food zapas żywności miasta (wspólny licznik do awansu, osadników i przejmowania pól)
 * @property autoClaimTiles czy miasto automatycznie przejmuje pola w zasięgu (jeśli nie, to gracz musi ręcznie przejmować pola)
 */
data class City(
    val id: Long,
    val name: String,
    val ownerId: Int,
    val position: HexCoordinate,
    val buildings: Set<BuildingId> = emptySet(),
    val ownedTiles: List<HexCoordinate> = emptyList(),
    val food: Int = 0,
    val autoClaimTiles: Boolean = true
    // TODO: dodać poziom miasta
) {
    init {
        require(id >= 0) { "id miasta nie może być ujemne" }
        require(name.isNotBlank()) { "nazwa miasta nie może być pusta" }
        require(ownerId >= 0) { "id gracza nie może być ujemne" }
        require(food >= 0) { "zapas żywności nie może być ujemny" }
        require(ownedTiles.distinct().size == ownedTiles.size) { "ownedTiles nie może zawierać duplikatów" }
        require(position !in ownedTiles) { "ownedTiles nie może zawierać pola centrum miasta" }
    }

    /** Centrum + zajęte pola */
    val allTiles: List<HexCoordinate> get() = listOf(position) + ownedTiles

    fun owns(tile: HexCoordinate): Boolean = tile == position || tile in ownedTiles

    fun hasBuilding(buildingId: BuildingId): Boolean = buildingId in buildings

    fun hasFeature(feature: BuildingFeature, ruleset: BuildingRuleset): Boolean =
        ruleset.hasFeature(buildings, feature)

    // Zajmowanie pól

    fun withoutTile(tile: HexCoordinate): City {
        require(tile in ownedTiles) { "Pole $tile nie należy do miasta (albo jest centrum)" }
        return copy(ownedTiles = ownedTiles - tile)
    }

    // Budynki

    fun canConstruct(building: BuildingDefinition, buildingRuleset: BuildingRuleset): Boolean =
        buildingRuleset.isAvailable(building.id, buildings)

    /** Dodaje budynek. Pobranie kosztu z zasobów gracza nie należy do odpowiedzialności tej funkcji. */
    fun withBuilding(building: BuildingDefinition, buildingRuleset: BuildingRuleset): City {
        require(canConstruct(building, buildingRuleset)) { "Miasto '$name' nie może zbudować budynku '${building.displayName}' (wymagania nie spełnione)" }
        return copy(buildings = buildings + building.id)
    }

    // Żywność

    fun withFood(newFood: Int): City {
        require(newFood >= 0) { "zapas żywności nie może być ujemny" }
        return copy(food = newFood)
    }
}
