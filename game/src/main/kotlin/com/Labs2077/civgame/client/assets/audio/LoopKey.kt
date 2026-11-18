package com.Labs2077.civgame.client.assets.audio

/**
 * Unikalny klucz konkretnej instancji dźwięku.
 *
 * Łączy źródło dźwięku z jego przeznaczeniem.
 */
data class LoopKey(
    val source: LoopSource,
    val slot: LoopSlot
)
