package com.Labs2077.civgame.engine.rules.city

import com.Labs2077.civgame.engine.city.CityLevelDefinition
import kotlinx.serialization.Serializable

/**
 * Reprezentuje plik JSON zawierający konfigurację poziomów miasta.
 *
 * @property schemaVersion wersja schematu pliku
 * @property buildings lista definicji poziomów miasta
 */
@Serializable
data class CityLevelRulesetFile(
    val schemaVersion: Int = 1,
    val levels: List<CityLevelDefinition>
)