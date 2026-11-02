package com.Labs2077.civgame.engine.rules.terrain

import com.Labs2077.civgame.engine.terrain.TerrainDefinition
import kotlinx.serialization.Serializable
/**
 * Reprezentuje plik JSON zawierający konfigurację rodzajów terenu.
 *
 * @property schemaVersion wersja struktury pliku konfiguracyjnego.
 * @property terrains lista definicji wszystkich dostępnych rodzajów terenu.
 */
@Serializable
data class TerrainRulesetFile(
    val schemaVersion: Int = 1,
    val terrains: List<TerrainDefinition>
)
