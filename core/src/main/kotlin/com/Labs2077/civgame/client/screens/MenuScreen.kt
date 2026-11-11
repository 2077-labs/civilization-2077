package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.ui.ColorSwatchButton
import com.Labs2077.civgame.client.ui.UiScreen
import com.Labs2077.civgame.client.units.requiredAnimations
import com.Labs2077.civgame.engine.PlayerSetup
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

/**
 * MOCK menu na Scene2D: wybór koloru dla dwóch graczy (ButtonGroup = "radio") i przycisk "Rozpocznij gre".
 * Przycisk jest wyłączony (disabled), gdy obaj gracze mają ten sam kolor (przykład użycia isDisabled; usuń, jeśli niepotrzebne).
 * Etykiety bez polskich znaków, bo domyślny BitmapFont ich nie ma.
 */
class MenuScreen(game: CivGame) : UiScreen(game) {
    companion object {
        private val PALETTE = intArrayOf(
            0x00E5FF,
            0xFFB000,
            0x50FF78,
            0xFF3B3B,
            0xF2F2F2,
            0x9B5CFF,
            0xFF7A00,
            0x14141C
        )
    }

    // wybrany kolor (indeks w PALETTE) gracza 1 i 2
    private val picks = intArrayOf(0, 3)
    private lateinit var startButton: TextButton
    private lateinit var messageLabel: Label

    override fun build(root: Table) {
        messageLabel = label("", wrap = true, color = Color(1f, 0.4f, 0.4f, 1f))
        startButton = TextButton("Rozpocznij grę", skin).apply {
            pad(14f, 40f, 14f, 40f)
            onClick { startGame() }
        }

        root.pad(60f)
            .top()
            .left()
        root.add(label("CYWILIZACJA 2077 - mock menu"))
            .colspan(2)
            .left()
            .padBottom(40f)
            .row()
        for (player in 0..1) {
            root.add(label("Gracz ${player + 1} - kolor"))
                .left()
                .padRight(40f)
            root.add(colorRow(player))
                .left()
                .padBottom(30f)
                .row()
        }
        root.add(startButton)
            .colspan(2)
            .left()
            .padTop(20f)
            .row()
        root.add(messageLabel)
            .colspan(2)
            .left()
            .width(780f)
            .padTop(20f)
            .row()

        refreshStart()
    }

    private fun colorRow(player: Int): Table {
        val group =
            ButtonGroup<ColorSwatchButton>().apply { setMinCheckCount(1); setMaxCheckCount(1) }
        val swatches = PALETTE.map { ColorSwatchButton(skin, it) }
        val row = Table()
        swatches.forEach {
            group.add(it); row.add(it)
            .size(56f)
            .pad(7f)
        }

        // początkowe zaznaczenie, zanim podpięte są listenery
        swatches[picks[player]].isChecked = true
        swatches.forEachIndexed { i, swatch ->
            swatch.onChecked { checked ->
                if (checked) {
                    picks[player] = i
                    refreshStart()
                }
            }
        }
        return row
    }

    private fun refreshStart() {
        val same = picks[0] == picks[1]
        startButton.isDisabled = same
        messageLabel.setText(if (same) "Gracze musza miec rozne kolory." else "")
    }

    private fun startGame() {
        val players = listOf(
            PlayerSetup(0, "Gracz 1", PALETTE[picks[0]]),
            PlayerSetup(1, "Gracz 2", PALETTE[picks[1]])
        )

        // wypieczenie atlasu jednostek i miast dla każdego gracza (tworzy tekstury na GPU, więc musi być w wątku renderowania)
        game.assets.startGame(players)
        // czy każda jednostka z reguł gry ma grafike? (po wypieczeniu, bo sprawdza wypieczone atlasy)
        val missing = game.assets.missingArt(game.unitRuleset.units.associate { unit ->
            unit.id to unit.requiredAnimations()
                .map { it.id }
        })
        if (missing.isNotEmpty()) {
            val text = "Brak grafik: " + missing.joinToString(", ")
            messageLabel.setText(text)
            Gdx.app.error("Assets", text)
            return
        }

        // TODO: tu powstanie silnik: Engine(ruleset z JSON, players, seed). Silnik dostaje tylko PlayerSetup.
        // przejście do gry (docelowo HexMapScreen(engine, game.assets) zamiast GameScreen)
        switchTo(GameScreen(game, players))
    }
}
