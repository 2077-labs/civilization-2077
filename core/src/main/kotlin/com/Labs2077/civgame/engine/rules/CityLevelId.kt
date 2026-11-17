package com.Labs2077.civgame.engine.rules

import kotlinx.serialization.Serializable

/**
 * Typowany identyfikator poziomu miasta w grze.
 *
 * @property value tekstowa wartość identyfikatora
 */
@Serializable
@JvmInline
value class CityLevelId(
    override val value: String
) : RuleId