
package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.ui.ColorSwatchButton
import com.Labs2077.civgame.client.ui.UiScreen
import com.Labs2077.civgame.client.units.requiredAnimations
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.CivilizationResources
import com.labs2077.civilization.core.models.Player

private const val MAX_PLAYER_SLOTS = 8

/**
 * Menu wyboru kolorów dla wszystkich graczy.
 *
 * Liczba graczy jest pobierana z NewGameSettings.
 * Każdy gracz musi mieć inny kolor.
 *
 * Logika wyboru kolorów znajduje się w MenuColorSelection,
 * dzięki czemu można ją testować niezależnie od interfejsu.
 */
class MenuScreen(
    game: CivGame,
    private val settings: NewGameSettings = NewGameSettings(),
) : UiScreen(game) {

    // Logika wyboru kolorów graczy.
    // Początkowo każdy gracz otrzymuje inny kolor.
    private val colorSelection = MenuColorSelection(
        initialColorIndices = (0 until settings.playerCount).toList()
    )

    private lateinit var startButton: TextButton
    private lateinit var messageLabel: Label

    override fun build(root: Table) {

        messageLabel = label(
            "",
            wrap = true,
            color = Color(1f, 0.4f, 0.4f, 1f)
        )

        startButton = TextButton("ROZPOCZNIJ GRĘ", skin, "primary").apply {
            pad(14f, 40f, 14f, 40f)
            onClick {
                startGame()
            }
        }

        // Główny panel ekranu.
        val panel = Table(skin).apply {
            background = skin.newDrawable(
                "white",
                Color(0.06f, 0.08f, 0.13f, 0.97f)
            )
            pad(36f)
        }

        // Nagłówek.
        val title = label(
            "WYBÓR GRACZY",
            color = Color.CYAN
        ).apply {
            setFontScale(1.9f)
        }

        val subtitle = label(
            "Wybierz kolor dla dwóch graczy. Pozostałe miejsca są zablokowane.",
            color = Color.LIGHT_GRAY
        )

        panel.add(title)
            .colspan(2)
            .left()
            .padBottom(8f)
            .row()

        panel.add(subtitle)
            .colspan(2)
            .left()
            .padBottom(28f)
            .row()

        // Siatka paneli graczy.
        val playersGrid = Table()

        // Pokazujemy osiem miejsc, ale dostępne są tylko dwa.
        for (player in 0 until MAX_PLAYER_SLOTS) {

            val isAvailable = player < settings.playerCount

            val playerPanel = Table(skin).apply {
                background = skin.newDrawable(
                    "white",
                    if (isAvailable) {
                        Color(0.10f, 0.12f, 0.18f, 1f)
                    } else {
                        Color(0.07f, 0.08f, 0.12f, 1f)
                    }
                )
                pad(12f)
            }

            val playerTitle = label(
                "GRACZ ${player + 1}",
                color = if (isAvailable) Color.CYAN else Color.GRAY
            ).apply {
                setFontScale(1.1f)
            }

            playerPanel.add(playerTitle)
                .center()
                .padBottom(10f)
                .row()

            if (isAvailable) {
                // Aktywne miejsce - można wybrać kolor.
                playerPanel.add(colorRow(player))
                    .center()
                    .row()
            } else {
                // Zablokowane miejsce - wyszarzone.
                playerPanel.add(
                    label("GRACZ ${player + 1}", color = Color.GRAY)
                ).center()
            }

            playersGrid.add(playerPanel)
                .width(250f)
                .height(if (isAvailable) 165f else 125f)
                .pad(6f)

            if ((player + 1) % 4 == 0) {
                playersGrid.row()
            }
        }
        // Komunikat o wyborze kolorów.
        panel.add(playersGrid)
            .colspan(2)
            .center()
            .padBottom(8f)
            .row()
        panel.add(messageLabel)
            .colspan(2)
            .width(700f)
            .left()
            .padTop(6f)
            .padBottom(12f)
            .row()

        // Przycisk powrotu do konfiguracji.
        val backButton = TextButton("WRÓĆ", skin).apply {
            pad(12f, 30f, 12f, 30f)

            onClick {
                switchTo(NewGameSettingsScreen(game, settings))
            }
        }

        val buttonsRow = Table()

        buttonsRow.add(backButton)
            .width(200f)
            .height(58f)
            .padRight(14f)

        buttonsRow.add(startButton)
            .width(280f)
            .height(58f)

        panel.add(buttonsRow)
            .colspan(2)
            .right()
            .padTop(12f)

        root.center()

        root.add(panel)
            .width(1160f)
            .pad(24f)

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

        swatches.forEachIndexed { index, swatch ->
            group.add(swatch)

            // Cztery kolory w jednym rzędzie.
            if (index > 0 && index % 4 == 0) {
                row.row()
            }

            row.add(swatch)
                .size(40f)
                .pad(4f)
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
     * Aktualizuje stan przycisku rozpoczęcia gry,
     * komunikat o błędnym wyborze kolorów
     * oraz informacje o wybranych kolorach graczy.
     */
    private fun refreshStart() {
        val canStart = colorSelection.canStart

        startButton.isDisabled = !canStart

        messageLabel.setText(
            if (canStart) {
                ""
            } else {
                "Gracze muszą mieć różne kolory."
            }
        )
    }

    /**
     * Przygotowuje graczy i rozpoczyna grę.
     */
    private fun startGame() {

        if (!colorSelection.canStart) {
            return
        }

        val civilizations = (0 until settings.playerCount).map { playerIndex ->
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

        game.assets.startGame(civilizations)

        val requiredAssets = game.unitRuleset.definitions.mapValues { (_, unitType) ->
            unitType.requiredAnimations().map { it.id }
        }

        val missingAssets = game.assets.missingArt(requiredAssets)
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







