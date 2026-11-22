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
import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.CivilizationResources
import com.labs2077.civilization.core.models.Player

/**
 * MOCK menu na Scene2D:
 * wybór kolorów dla dwóch graczy oraz przycisk rozpoczęcia gry.
 *
 * Logika wyboru kolorów znajduje się w MenuColorSelection,
 * dzięki czemu można ją testować niezależnie od interfejsu.
 */
class MenuScreen(game: CivGame) : UiScreen(game) {

    // Logika wyboru kolorów graczy.
    private val colorSelection = MenuColorSelection()

    private lateinit var startButton: TextButton
    private lateinit var messageLabel: Label

    override fun build(root: Table) {
        messageLabel = label("", wrap = true, color = Color(1f, 0.4f, 0.4f, 1f))

        startButton = TextButton("Rozpocznij gre", skin).apply {
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

    /**
     * Tworzy wiersz przycisków z kolorami dla danego gracza.
     */
    private fun colorRow(player: Int): Table {
        val group = ButtonGroup<ColorSwatchButton>().apply {
            setMinCheckCount(1)
            setMaxCheckCount(1)
        }

        // Tworzymy przyciski na podstawie palety z MenuColorSelection.
        val swatches = colorSelection.palette.map {
            ColorSwatchButton(skin, it)
        }

        val row = Table()

        swatches.forEach {
            group.add(it)
            row.add(it)
                .size(56f)
                .pad(7f)
        }

        // Ustawiamy początkowo wybrany kolor gracza.
        swatches[colorSelection.selectedColorIndex(player)].isChecked = true

        // Reagujemy na zmianę zaznaczonego koloru.
        swatches.forEachIndexed { i, swatch ->
            swatch.onChecked { checked ->
                if (checked) {
                    colorSelection.selectColor(player, i)
                    refreshStart()
                }
            }
        }

        return row
    }

    /**
     * Aktualizuje stan przycisku rozpoczęcia gry
     * oraz komunikat informujący o błędnym wyborze kolorów.
     */
    private fun refreshStart() {
        val canStart = colorSelection.canStart

        startButton.isDisabled = !canStart

        messageLabel.setText(
            if (canStart) {
                ""
            } else {
                "Gracze musza miec rozne kolory."
            }
        )
    }

    /**
     * Przygotowuje graczy i rozpoczyna grę.
     */
    private fun startGame() {
        // Nie pozwalamy rozpocząć gry, jeśli gracze mają ten sam kolor.
        if (!colorSelection.canStart) {
            return
        }

        // TODO: tu powstanie silnik: Engine(ruleset z JSON, players, seed).
        val civilizations = (0 until colorSelection.playerCount).map { playerIndex ->
            Civilization(
                player = Player(),
                color = com.labs2077.civilization.core.models.Color(
                    colorSelection.selectedColor(playerIndex)
                ),
                cities = emptyList(),
                units = emptyList(),
                resources = CivilizationResources(),
            )
        }

        // Wypieczenie atlasu jednostek i miast dla każdego gracza.
        // Tworzy tekstury na GPU, więc musi odbywać się w wątku renderowania.
        game.assets.startGame(civilizations)

        // Sprawdzamy, czy każda jednostka z reguł gry ma grafikę.
        // Kontrola odbywa się po wypieczeniu atlasów.
        val unitTypes = game.unitRuleset.definitions

        val requiredAssets = unitTypes.mapValues { (_, unitType) ->
            unitType.requiredAnimations()
                .map { it.id }
        }
        val missingAssets = game.assets.missingArt(
            requiredAssets
        )
        val missingDecorations = game.assets.missingDecorations(
            game.decorationRuleset.decorationIds
        )

        if (missingAssets.isNotEmpty()) {
            val text = "Brak grafik: " + missingAssets.joinToString(", ")
            messageLabel.setText(text)
            Gdx.app.error("Assets", text)
            return
        } else if (missingDecorations.isNotEmpty()) {
            val text = "Brak grafik dekoracji: " + missingDecorations.joinToString(", ")
            messageLabel.setText(text)
            Gdx.app.error("Assets", text)
            return
        }

        switchTo(GameScreen(game, civilizations))
    }
}
