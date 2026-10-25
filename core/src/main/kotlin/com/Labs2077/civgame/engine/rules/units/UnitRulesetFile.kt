package com.Labs2077.civgame.engine.rules.units

import com.Labs2077.civgame.engine.units.UnitDefinition
import kotlinx.serialization.Serializable

@Serializable
data class UnitRulesetFile(
    val schemaVersion: Int = 1,
    val units: List<UnitDefinition>
)
