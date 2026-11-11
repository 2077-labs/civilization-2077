package com.Labs2077.civgame.client.assets.audio

/**
 * Określa przeznaczenie konkretnej instancji dźwięku.
 *
 * Jedno źródło może posiadać kilka niezależnych pętli.
 */
enum class LoopSlot {
    MOVEMENT,
    IDLE,
    WEAPON,
    AMBIENCE,
    WEATHER
}
