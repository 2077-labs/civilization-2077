
package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.rendering.HexMenuBackground
import com.Labs2077.civgame.client.ui.UiScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

class MainMenuScreen(game: CivGame) : UiScreen(game) {

    // Renderer odpowiedzialny za rysowanie heksów w tle menu
    private val hexBackground = HexMenuBackground(game.assets)

    override fun build(root: Table) {

        // Główny panel menu
        val panel = Table(skin).apply {
            background = skin.newDrawable(
                "white",
                Color(0.08f, 0.10f, 0.16f, 0.88f)
            )
            pad(48f)
        }

        // Tytuł gry
        val title = label(
            "CYWILIZACJA 2077",
            color = Color.CYAN
        ).apply {
            setFontScale(2.2f)
        }

        // Przycisk rozpoczęcia nowej gry
        val newGameButton = TextButton("NOWA GRA", skin, "primary").apply {
            pad(16f, 40f, 16f, 40f)
            onClick {
                switchTo(NewGameSettingsScreen(game))
            }
        }

        // Przycisk ustawień - funkcjonalność dodamy później
        val settingsButton = TextButton("USTAWIENIA", skin).apply {
            isDisabled = true
        }

        // Przycisk wyjścia z gry
        val exitButton = TextButton("WYJŚCIE", skin).apply {
            pad(12f, 40f, 12f, 40f)
            onClick {
                Gdx.app.exit()
            }
        }

        // Układamy elementy w głównym panelu
        panel.add(title)
            .center()
            .padBottom(48f)
            .row()

        panel.add(newGameButton)
            .width(360f)
            .height(62f)
            .padBottom(18f)
            .row()

        panel.add(settingsButton)
            .width(360f)
            .height(54f)
            .padBottom(18f)
            .row()

        panel.add(exitButton)
            .width(360f)
            .height(54f)
            .row()

        root.center()
        root.add(panel)
            .width(680f)
            .pad(30f)
    }

    // Rysujemy heksy przed wyświetleniem elementów interfejsu
    override fun drawBackground(delta: Float) {
        hexBackground.render(
            stage.camera.combined,
            stage.root.color.a
        )
    }

    // Zwalniamy zasoby renderera po opuszczeniu menu
    override fun hide() {
        hexBackground.dispose()
        super.hide()
    }
}
