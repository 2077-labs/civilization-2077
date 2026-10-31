package com.Labs2077.civgame.engine.rules

import kotlinx.serialization.Serializable

/**
 * Typowany identyfikator jednostki
 *
 * @property value tekstowa wartość identyfikatora
 */
@Serializable
@JvmInline
value class UnitId(
    override val value: String
) : RuleId
