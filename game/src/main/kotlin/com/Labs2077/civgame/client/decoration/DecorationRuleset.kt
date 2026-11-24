package com.Labs2077.civgame.client.decoration

import com.labs2077.civilization.core.models.AbstractRuleset
import com.labs2077.civilization.core.models.TerrainDefinitionId
import com.labs2077.civilization.core.models.TerrainRuleset

@JvmInline
value class DecorationId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) {
            "Identyfikator definicji dekoracji nie może być pusty."
        }
    }

    val regionName: String
        get() = "deco_$value"
}

/** Reguła losowania jednej dekoracji. decorationId bez prefiksu "deco_" (region "deco_scrap_car" -> "scrap_car"). */
data class DecorationRule(
    val decorationId: DecorationId,
    val weight: Int,
    val maxPerTile: Int = 1
) {
    init {
        require(decorationId.value.matches(ID_REGEX)) { "Niepoprawne decorationId: '$decorationId'." }
        require(weight > 0) { "Waga (weight) musi być dodatnia: $weight ($decorationId)." }
        require(maxPerTile >= 1) { "maxPerTile musi być >= 1: $maxPerTile ($decorationId)." }
    }

    private companion object {
        val ID_REGEX = Regex("[a-z][a-z0-9_]*")
    }
}

/**
 * Jak dekorować jeden rodzaj terenu.
 * @property terrainId identyfikator rodzaju terenu
 * @property chance szansa (0..1), że kafelek w ogóle dostanie dekoracje
 * @property minCount minimalna liczba dekoracji na kafelku, o ile kafelek je dostał
 * @property maxCount maksymalna liczba dekoracji (włącznie)
 * @property rules reguły losowania dekoracji
 */
data class TerrainDecorations(
    val terrainId: TerrainDefinitionId,
    val chance: Float,
    val minCount: Int,
    val maxCount: Int,
    val rules: List<DecorationRule>
) {
    init {
        require(chance in 0f..1f) { "chance musi być w 0..1: $chance (${terrainId.value})." }
        require(minCount >= 0 && maxCount >= minCount) {
            "Wymagane 0 <= minCount <= maxCount (${terrainId.value})."
        }
        require(rules.isNotEmpty()) { "Lista rules nie może być pusta (${terrainId.value})." }
    }
}

class DecorationRuleset(definitions: List<TerrainDecorations>) :
    AbstractRuleset<TerrainDefinitionId, TerrainDecorations>(
        definitions,
        keySelector = { it.terrainId }) {
    /** Wszystkie identyfikatory dekoracji, które są używane w regułach. (do sprawdzenia, czy wszystkie mają grafiki) */
    val decorationIds: Set<DecorationId>
        get() = definitions.values.flatMapTo(linkedSetOf()) { t -> t.rules.map { it.decorationId } }

    /** Każdy teren z decorations.json musi istnieć w terrains.json. */
    fun validateGains(terrains: TerrainRuleset) {
        val unknown = definitions.keys - terrains.definitions.keys
        require(unknown.isEmpty()) {
            "decorations.json zawiera definicje dla nieznanych terenów: ${unknown.joinToString(", ")}"
        }
    }
}
