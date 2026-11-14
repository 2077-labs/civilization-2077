package com.Labs2077.civgame.engine.rules.buildings

import com.Labs2077.civgame.engine.buildings.BuildingDefinition
import kotlinx.serialization.Serializable

/**
 * Reprezentuje plik JSON zawierający konfigurację budynków.
 *
 * @property schemaVersion wersja schematu pliku
 * @property buildings lista definicji budynków
 */
@Serializable
data class BuildingRulesetFile (
    val schemaVersion: Int = 1,
    val buildings: List<BuildingDefinition> = emptyList()
)
