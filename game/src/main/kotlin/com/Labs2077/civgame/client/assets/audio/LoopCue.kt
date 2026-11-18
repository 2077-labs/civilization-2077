package com.Labs2077.civgame.client.assets.audio

/**
 * Rodzaje zapętlonych dźwięków. W odróżnieniu od AudioCue nie mają wariantów: jedna pętla = jeden plik,
 * więc mapowanie na SfxId jest stałe i sprawdzane przez kompilator.
 */
enum class LoopCue(val sfx: SfxId) {
    ENGINE(SfxId.ENGINE_LOOP),
    GEIGER(SfxId.GEIGER_LOOP),
    RAIN(SfxId.RAIN_LOOP),
    SANDSTORM(SfxId.SANDSTORM_LOOP)
}
