package com.Labs2077.civgame.engine.rules.units

import com.Labs2077.civgame.engine.rules.RuleId
import kotlinx.serialization.Serializable

/**
 * Typowany identyfikator budynku
 *
 * @property value tekstowa wartość identyfikatora
 */
@Serializable
@JvmInline
value class BuildingId(
    override val value: String
) : RuleId
