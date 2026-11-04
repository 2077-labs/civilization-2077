package com.Labs2077.civgame.client.assets.audio

import com.Labs2077.civgame.client.assets.GameAssets
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound

/** Opis pętli, która ma grać (do LoopPlayer.sync). */
data class LoopRequest(val cue: LoopCue, val volume: Float = 1f)

/**
 * Zarządza zapętlonymi dźwiękami: pamięta, które pętle grają (po LoopKey), nie dopuszcza duplikatów,
 * pozwala je zatrzymać pojedynczo, całe źródło albo wszystkie naraz, i potrafi robić krótkie wyciszanie przy zatrzymaniu.
 * Dwa style użycia:
 *  - zdarzeniowy: start(key, cue) przy początku ruchu, stop(key) przy końcu.
 *  - stanowy: co klatkę (albo przy zmianie stanu) sync(opis tego, co ma grać); reszta jest zatrzymywana.
 *    Ten drugi sposób użycia może uodpornić grę na błędy w logice, bo nie trzeba pamiętać, co już gra, a LoopPlayer sam zatrzyma to, czego nie ma w sync.
 */
class LoopPlayer(private val soundOf: (SfxId) -> Sound, private val maxLoops: Int = 12) {
    private companion object { const val TAG = "LoopPlayer" }

    private class Active(val cue: LoopCue, val instanceId: Long, var volume: Float)
    private class Fade(val loopKey: LoopKey, val cue: LoopCue, val instanceId: Long, val startVolume: Float, val duration: Float) {
        var elapsed = 0f
    }

    private val active = HashMap<LoopKey, Active>()
    private val fading = ArrayList<Fade>()

    /** Głośność główna (np. z ustawień gracza), mnożona z głośnością każdej pętli. */
    var masterVolume: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            active.values.forEach { applyVolume(it) }
        }

    fun isPlaying(key: LoopKey): Boolean = key in active
    fun isFading(key: LoopKey): Boolean = fading.any { it.loopKey == key }

    /** Uruchamia pętlę. Jeśli ta sama pętla już gra pod tym kluczem, tylko aktualizuje głośność. */
    fun start(key: LoopKey, cue: LoopCue, volume: Float = 1f) {
        val current = active[key]
        if (current != null) {
            if (current.cue == cue) { setVolume(key, volume); return }
            // inny dźwięk pod tym kluczem, więc zatrzymujemy stary i uruchamiamy nowy
            stop(key)
        }
        if (active.size >= maxLoops) {
            Gdx.app.log(TAG, "Limit pętli ($maxLoops) osiągnięty, pomijam $key")
            return
        }
        val id = soundOf(cue.sfx).loop(volume * masterVolume)
        // id == -1 oznacza brak wolnego kanału audio (Sound.play() zwraca -1, gdy nie ma wolnego kanału)
        if (id == -1L) {
            Gdx.app.log(TAG, "Brak wolnego kanału audio dla $cue")
            return
        }
        active[key] = Active(cue, id, volume)
    }

    fun setVolume(key: LoopKey, volume: Float) {
        val a = active[key] ?: return
        a.volume = volume
        applyVolume(a)
    }

    /**
     * Zatrzymuje pętlę; z fadeSeconds > 0 wycisza ją w tym czasie (update() domyka wyciszanie).
     */
    fun stop(key: LoopKey, fadeSeconds: Float = 0f) {
        val a = active.remove(key) ?: return
        if (fadeSeconds <= 0f) soundOf(a.cue.sfx).stop(a.instanceId)
        else fading += Fade(key, a.cue, a.instanceId, a.volume * masterVolume, fadeSeconds)
    }

    /** Zatrzymuje wszystkie pętle jednego źródła (np. jednostki, która zginęła). */
    fun stopSource(source: LoopSource, fadeSeconds: Float = 0f) {
        active.keys.filter { it.source == source }.forEach { stop(it, fadeSeconds) }
    }

    fun stopAll() {
        active.keys.toList().forEach { stop(it) }
        fading.forEach { soundOf(it.cue.sfx).stop(it.instanceId) }
        fading.clear()
    }

    /**
     * Uruchamia (lub aktualizuje) wszystko z [desired], a pętle z zakresu [scope],
     * których tam nie ma, zatrzymuje. Zakres pozwala niezależnym systemom (jednostki, pogoda) nie zatrzymywać swoich pętli.
     */
    fun sync(desired: Map<LoopKey, LoopRequest>, scope: (LoopKey) -> Boolean = { true }, fadeSeconds: Float = 0.15f) {
        for ((key, request) in desired) start(key, request.cue, request.volume)
        active.keys.filter { scope(it) && it !in desired }.forEach { stop(it, fadeSeconds) }
    }

    fun pauseAll() {
        active.values.forEach { soundOf(it.cue.sfx).pause(it.instanceId) }
        fading.forEach { soundOf(it.cue.sfx).pause(it.instanceId) }
    }

    fun resumeAll() {
        active.values.forEach { soundOf(it.cue.sfx).resume(it.instanceId) }
        fading.forEach { soundOf(it.cue.sfx).resume(it.instanceId) }
    }

    /** Wołać co klatkę: obsługuje wyciszanie zatrzymywanych pętli. */
    fun update(delta: Float) {
        val iterator = fading.iterator()
        while (iterator.hasNext()) {
            val fade = iterator.next()
            fade.elapsed += delta
            if (fade.elapsed >= fade.duration) {
                soundOf(fade.cue.sfx).stop(fade.instanceId)
                iterator.remove()
            } else {
                val left = 1f - fade.elapsed / fade.duration
                soundOf(fade.cue.sfx).setVolume(fade.instanceId, fade.startVolume * left)
            }
        }
    }

    private fun applyVolume(a: Active) {
        soundOf(a.cue.sfx).setVolume(a.instanceId, a.volume * masterVolume)
    }
}
