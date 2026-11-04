package com.Labs2077.civgame.client.assets

import com.Labs2077.civgame.client.assets.audio.SfxId
import com.Labs2077.civgame.engine.PlayerSetup
import com.Labs2077.civgame.engine.rules.TerrainId
import com.Labs2077.civgame.engine.rules.UnitId
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable

/**
 * Renderer i UI pytają tę klasę o zasoby po id ("tank", "desert_terrain"), a silnik w ogóle jej nie widzi.
 */
class GameAssets() : Disposable, AssetProvider {
    private companion object {
        const val COMMON_ATLAS = "atlas/common.atlas"
        // Jednostki i miasta są w jednym atlasie, bo miasta mają sztandar w magencie, więc razem z jednostkami przechodzą wypiekanie kolorów graczy.
        const val UNITS_ATLAS = "atlas/units.atlas"
        // region zastępczy w common.atlas (szachownica magenta-czerń)
        const val MISSING = "missing"
        const val FRAME_TIME = 0.12f
    }

    private val am = AssetManager()
    val skins = FactionSkins(Gdx.files.internal(UNITS_ATLAS))

    private lateinit var common: TextureAtlas
    private val missing: TextureAtlas.AtlasRegion by lazy {
        common.findRegion(MISSING) ?: error("Brak regionu '$MISSING' w $COMMON_ATLAS (dodaj raw/common/missing.png i spakuj atlas)")
    }
    private var baseReady = false

    /** ładowanie wspólnych zasobów (atlas common, dźwięki). Wywoływane w create() gry przed ekranem ładowania */
    fun queueBase() {
        am.load(COMMON_ATLAS, TextureAtlas::class.java)
        SfxId.ALL.forEach { am.load((it.path), Sound::class.java) }
    }

    /** Wołać co klatkę na ekranie ładowania. Zwraca true, gdy wszystko gotowe. */
    fun updateLoading(): Boolean {
        val done = am.update()
        if (done && !baseReady) {
            common = am.get(COMMON_ATLAS, TextureAtlas::class.java)
            baseReady = true
        }
        return done
    }

    val progress: Float get() = am.progress

    /** Start gry. Wołać po zakończeniu ładowania i wybraniu danych graczy w menu. */
    fun startGame(players: List<PlayerSetup>) = skins.bake(players)

    /** Atlasy z AssetManager odtworzą się same, wypieczone trzeba odbudować. */
    fun onResume() = skins.rebuild()

    /** Dostęp dla renderera (zawsze po id). Brak grafiki daje region "missing" */
    override fun terrain(id: TerrainId): TextureRegion = common.findRegion("terrain_${id.value}") ?: missing
    override fun icon(id: String): TextureRegion = common.findRegion("icon_$id") ?: missing
    override fun unitFrames(playerId: Int, unitId: UnitId, anim: String): Array<out TextureRegion> =
        framesOrMissing(skins.frames(playerId, "${unitId.value}_$anim"))
    override fun cityFrames(playerId: Int, level: Int): Array<out TextureRegion> =
        framesOrMissing(skins.frames(playerId, "settlement_lvl$level"))
    override fun fxFrames(id: String): Array<out TextureRegion> = framesOrMissing(common.findRegions("fx_$id"))

    /** Animacje powinny być tworzone raz (np. w show() ekranu), a nie w render(). attack i efekty zwykle z PlayMode.NORMAL. */
    override fun unitAnimation(playerId: Int, unitId: UnitId, anim: String,
                      mode: Animation.PlayMode): Animation<TextureRegion> =
        Animation<TextureRegion>(FRAME_TIME, unitFrames(playerId, unitId, anim), mode)
    override fun cityAnimation(playerId: Int, level: Int): Animation<TextureRegion> =
        Animation<TextureRegion>(FRAME_TIME, cityFrames(playerId, level), Animation.PlayMode.LOOP)
    override fun sound(sound: SfxId): Sound = am.get(sound.path, Sound::class.java)


    /**
     * Łączy dwa niezależne światy: id jednostek z reguł gry (silnik) i regiony w atlasie (klient).
     * required: id jednostki -> animacje, które ma mieć (jednostki cywilne bez attack).
     * Zwraca nazwy brakujących regionów. Do wywołania po startGame() przed rozpoczęciem rozgrywki.
     */
    fun missingArt(required: Map<UnitId, Collection<String>>): List<String> =
        required.flatMap { (unit, anims) -> anims.map { "${unit.value}_$it" } }.filterNot { skins.hasFrames(it) }

    // TODO: To be removed
    fun finishLoading() {
        am.finishLoading()
    }

    private fun framesOrMissing(frames: Array<out TextureRegion>): Array<out TextureRegion> =
        if (frames.size > 0) frames else Array.with(missing)

    override fun dispose() {
        skins.dispose()
        am.dispose()
    }
}
