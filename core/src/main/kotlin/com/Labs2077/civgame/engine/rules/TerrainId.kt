package com.Labs2077.civgame.engine.rules

import kotlinx.serialization.Serializable

/**
 * Typowany identyfikator rodzaju terenu
 *
 * @property value tekstowa wartość identyfikatora
 */
@Serializable
@JvmInline
value class TerrainId(
    override val value: String
) : RuleId
