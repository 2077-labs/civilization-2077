package com.Labs2077.civgame.client.assets.audio

import com.Labs2077.civgame.client.assets.GameAssets
import com.badlogic.gdx.audio.Sound

/**
 * Jedyny punkt wejścia do dźwięków w grze: ekrany i renderer nie dotykają ani GameAssets, ani LoopPlayer.
 *
 *   audio.play(AudioCue.RIFLE_FIRE)                              jednorazowe (losowy wariant z listy)
 *   audio.startLoop(key, LoopCue.ENGINE) / stopLoop(key)         zapętlone, sterowane zdarzeniami
 *   audio.syncLoops(desired, scope)                              zapętlone, opisem stanu docelowego, porzuca wszelkie inne nieokreślone pętle
 */
class GameAudio(
    private val soundOf: (SfxId) -> Sound
) {
    private val sounds = mapOf(
        AudioCue.UI_CLICK to listOf(SfxId.UI_CLICK),
        AudioCue.TURN_END to listOf(SfxId.TURN_END),

        AudioCue.BUILD_COMPLETED to listOf(SfxId.BUILD_DONE),
        AudioCue.CITY_FOUNDED to listOf(SfxId.CITY_FOUNDED),

        AudioCue.RIFLE_FIRE to listOf(SfxId.RIFLE_SHOT, SfxId.RIFLE_SHOT_B),
        AudioCue.LASER_FIRE to listOf(SfxId.LASER_SHOT, SfxId.LASER_SHOT_B),
        AudioCue.ROCKET_FIRE to listOf(SfxId.ROCKET_LAUNCH),
        AudioCue.TANK_FIRE to listOf(SfxId.TANK_CANNON),

        AudioCue.UNIT_HIT to listOf(SfxId.UNIT_HIT, SfxId.UNIT_HIT_B),
        AudioCue.EXPLOSION to listOf(SfxId.EXPLOSION),

        AudioCue.MECH_STEP to listOf(SfxId.MECH_STEP, SfxId.MECH_STEP_B),
        AudioCue.RADAR_PING to listOf(SfxId.RADAR_PING),

        AudioCue.ERROR to listOf(SfxId.ERROR_BUZZ)
    )

    private val loops = LoopPlayer(soundOf)

    init {
        val unmapped = AudioCue.ALL
            .filter { it !in sounds.keys }
        require(unmapped.isEmpty()) { "GameAudio: brak zmapowanych dźwięków dla: $unmapped" }
    }

    /** Głośność główna (np. z ustawień gracza), mnożona z głośnością każdej pętli. */
    var volume: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            loops.masterVolume = field
        }

    /** Uruchamia dźwięk jednorazowy. */
    fun play(cue: AudioCue, volume: Float = 1f) {
        soundOf(sounds.getValue(cue).random()).play(volume * this.volume)
    }

    /** Zapętlone dźwięki sterowane zdarzeniami */
    fun startLoop(key: LoopKey, cue: LoopCue, volume: Float = 1f) = loops.start(key, cue, volume)

    fun setLoopVolume(key: LoopKey, volume: Float) = loops.setVolume(key, volume)

    fun stopLoop(key: LoopKey, fadeSeconds: Float = 0f) = loops.stop(key, fadeSeconds)

    /** Zatrzymuje wszystkie pętle jednego źródła (np. jednostki, która zginęła). */
    fun stopLoopsOf(source: LoopSource, fadeSeconds: Float = 0f) = loops.stopSource(source, fadeSeconds)

    fun stopAllLoops() = loops.stopAll()

    /** Styl stanowy: uruchamia lub aktualizuje wszystko z [desired], a pętle z zakresu [scope], które nie są w [desired], zatrzymuje. */
    fun syncLoops(
        desired: Map<LoopKey, LoopRequest>,
        scope: (LoopKey) -> Boolean = { true },
        fadeSeconds: Float = 0.15f
    ) = loops.sync(desired, scope, fadeSeconds)

    fun isLoopPlaying(key: LoopKey): Boolean = loops.isPlaying(key)
    fun isLoopFading(key: LoopKey): Boolean = loops.isFading(key)

    /** Cykl życia: update() musi być wywoływane w render() */
    fun pause() = loops.pauseAll()
    fun resume() = loops.resumeAll()
    fun update(delta: Float) = loops.update(delta)

    fun dispose() = loops.stopAll()
}
