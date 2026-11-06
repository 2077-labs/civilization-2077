package com.Labs2077.civgame.client.ui

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.assets.audio.AudioCue
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.min

/**
 * Warstwa abstrakcji nad Scene2D (Stage). Bierze na siebie to, co powtarza się w każdym ekranie z UI:
 *  - Stage z viewportem 1280x720 i Skin zbudowany w kodzie (UiSkinFactory) ze wspólną czcionką z GameAssets,
 *  - obsługa wejścia przez Stage (przyciski, hover, disabled, grupy przycisków robi Scene2D),
 *  - zwalnianie zasobów w hide(),
 *  - płynne wejście (fade in) całego Stage'a i bezpieczną zmianę ekranu z wyjściem (switchTo),
 *  - dźwięk kliknięcia w jednym miejscu oraz krótkie pomocnicze funkcje onClick / onChecked.
 *
 * Podklasa implementuje build(root), gdzie układa widgety w tabeli (Table). Dla ekranów, które rysują też świat
 * gry (np. mapa lub sprite'y), są dodatkowe funkcje: drawBackground() + drawWorld { } (rysowanie pod UI, razem z fade) oraz inputs
 * (do dodania własnych InputProcessorów, np. gesty mapy; Stage dostaje wejście jako pierwszy).
 */
abstract class UiScreen(protected val game: CivGame) : ScreenAdapter() {
    protected lateinit var stage: Stage
    protected lateinit var skin: Skin
    protected lateinit var root: Table
    protected val inputs = InputMultiplexer()
    protected open val background = Color(0.07f, 0.08f, 0.12f, 1f)

    private var leaving = false

    // do implementacji w podklasach

    /** Układa widgety w [root] (tabela wypełniająca cały ekran). */
    protected abstract fun build(root: Table)

    /** Logika ekranu wołana co klatkę przed rysowaniem. */
    protected open fun update(delta: Float) {}

    /** Rysowanie pod UI (np. sprite'y lub mapa). Trzeba użyć drawWorld { batch -> ... }. Stage rysuje się po tym wywołaniu. */
    protected open fun drawBackground(delta: Float) {}

    // cykl życia

    final override fun show() {
        skin = UiSkinFactory.create(game.assets.uiFont)
        stage = Stage(FitViewport(1280f, 720f))
        root = Table().apply { setFillParent(true) }
        stage.addActor(root)
        build(root)

        stage.root.color.a = 0f
        // animacja wejścia całego ekranu
        stage.root.addAction(Actions.fadeIn(0.25f))

        inputs.clear()
        inputs.addProcessor(stage)
        Gdx.input.inputProcessor = inputs
    }

    final override fun render(delta: Float) {
        update(delta)
        ScreenUtils.clear(background.r, background.g, background.b, background.a)
        stage.viewport.apply()
        drawBackground(delta)
        stage.act(min(delta, 1f / 30f))
        stage.draw()
    }

    final override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {
        if (Gdx.input.inputProcessor === inputs) Gdx.input.inputProcessor = null
        stage.dispose()
        skin.dispose()
    }

    // możliwość rysowania świata pod UI

    /**
     * Rysuje sprite'y we współrzędnych Stage'a (1280x720) wspólnym SpriteBatch Stage'a, z tą samą przezroczystością co UI,
     * więc świat też wygasza się i pojawia razem z ekranem.
     */
    protected fun drawWorld(block: (Batch) -> Unit) {
        val batch = stage.batch
        batch.projectionMatrix = stage.camera.combined
        batch.setColor(1f, 1f, 1f, stage.root.color.a)
        batch.begin()
        block(batch)
        batch.end()
        batch.setColor(Color.WHITE)
    }

    // nawigacja

    /**
     * Przechodzi do innego ekranu: najpierw wygaszany jest obecny stage, potem zmiana ekranu.
     */
    protected fun switchTo(screen: Screen, fadeSeconds: Float = 0.2f) {
        // jeśli już wychodzimy, nie wchodzimy w kolejną animację i nie zmieniamy ekranu
        if (leaving) return
        leaving = true
        stage.root.touchable = Touchable.disabled
        stage.root.addAction(
            Actions.sequence(
                Actions.fadeOut(fadeSeconds),
                Actions.run { Gdx.app.postRunnable { game.screen = screen } }
            )
        )
    }

    // funkcje pomocnicze

    protected fun label(text: String, wrap: Boolean = false, color: Color = Color.WHITE): Label =
        Label(text, skin).apply {
            setWrap(wrap)
            this.color = color
        }

    protected fun playClick() = game.gameAudio.play(AudioCue.UI_CLICK, 0.6f)

    /**
     * Kliknięcie przycisku z dźwiękiem pod UI
     */
    protected fun Button.onClick(sound: Boolean = true, action: () -> Unit) {
        addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (sound) playClick()
                action()
            }
        })
    }

    /**
     * Zmiana zaznaczenia przycisku (np. w ButtonGroup - MenuScreen).
     */
    protected fun Button.onChecked(sound: Boolean = true, action: (Boolean) -> Unit) {
        val button = this
        addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (sound && button.isChecked) playClick()
                action(button.isChecked)
            }
        })
    }
}
