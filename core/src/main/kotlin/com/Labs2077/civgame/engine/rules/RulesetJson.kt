package com.Labs2077.civgame.engine.rules

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

/**
 * Klasa pomocnicza do serializacji i deserializacji rulesetów w formacie JSON.
 */
object RulesetJson {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
        isLenient = false
    }

    fun <T> decode(
        source: String,
        deserializer: DeserializationStrategy<T>
    ): T = json.decodeFromString(deserializer, source)

    fun <T> encode(
        value: T,
        serializer: SerializationStrategy<T>
    ): String = json.encodeToString(serializer, value)
}
