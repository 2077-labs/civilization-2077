
package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.ui.UiScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField

class NewGameSettingsScreen(
    game: CivGame,
    private val initialSettings: NewGameSettings = NewGameSettings(),
) : UiScreen(game) {

    private var selectedWorldSize = initialSettings.worldSize
    private var selectedPlayerCount = initialSettings.playerCount

    private lateinit var seedField: TextField
    private lateinit var messageLabel: Label
    private lateinit var sizeSummaryLabel: Label
    private lateinit var playerCountSummaryLabel: Label

    private val sizeButtons = linkedMapOf<WorldSizeOption, TextButton>()
    private val sizeCards = linkedMapOf<WorldSizeOption, Table>()
    private val playerCountButtons = linkedMapOf<Int, TextButton>()

    override fun build(root: Table) {

        // Główny panel konfiguracji.
        val panel = Table(skin).apply {
            background = skin.newDrawable(
                "white",
                Color(0.06f, 0.08f, 0.13f, 0.97f)
            )
            pad(24f)
        }

        // Nagłówek.
        val title = label(
            "NOWA GRA",
            color = Color.CYAN
        ).apply {
            setFontScale(1.9f)
        }

        panel.add(title)
            .colspan(2)
            .left()
            .padBottom(6f)
            .row()

        panel.add(
            label(
                "Przygotuj świat przed rozpoczęciem rozgrywki.",
                color = Color.LIGHT_GRAY
            )
        )
            .colspan(2)
            .left()
            .padBottom(18f)
            .row()

        // Dwie kolumny z ustawieniami.
        val optionsRow = Table()

        // LEWA KOLUMNA - rozmiar mapy.
        val worldSection = Table(skin).apply {
            background = skin.newDrawable(
                "white",
                Color(0.10f, 0.12f, 0.18f, 1f)
            )
            pad(14f)
        }

        worldSection.add(
            label("ROZMIAR ŚWIATA", color = Color.CYAN)
        )
            .left()
            .padBottom(12f)
            .row()

        val sizeGroup = ButtonGroup<TextButton>().apply {
            setMinCheckCount(1)
            setMaxCheckCount(1)
        }

        val cardsRow = Table()

        WorldSizeOption.values().forEach { option ->

            val card = Table(skin).apply {
                background = skin.newDrawable(
                    "white",
                    Color(0.11f, 0.14f, 0.20f, 1f)
                )
                pad(7f)
            }

            val button = TextButton(
                option.displayName.uppercase(),
                skin
            )

            sizeGroup.add(button)
            sizeButtons[option] = button
            sizeCards[option] = card

            button.onChecked { checked ->
                if (checked) {
                    selectedWorldSize = option
                    updateSizeSelection()
                }
            }

            card.add(button)
                .width(112f)
                .height(38f)
                .row()

            card.add(
                label(
                    "${option.width} x ${option.height}",
                    color = Color.CYAN
                )
            )
                .padTop(7f)

            cardsRow.add(card)
                .width(138f)
                .height(82f)
                .padRight(4f)
        }

        worldSection.add(cardsRow)
            .left()
            .row()

        sizeSummaryLabel = label(
            "",
            color = Color.LIGHT_GRAY
        )

        worldSection.add(sizeSummaryLabel)
            .left()
            .padTop(10f)

        // PRAWA KOLUMNA - liczba graczy.
        val playersSection = Table(skin).apply {
            background = skin.newDrawable(
                "white",
                Color(0.10f, 0.12f, 0.18f, 1f)
            )
            pad(14f)
        }

        playersSection.add(
            label("LICZBA GRACZY", color = Color.CYAN)
        )
            .left()
            .padBottom(12f)
            .row()

        val playerCountGroup = ButtonGroup<TextButton>().apply {
            setMinCheckCount(1)
            setMaxCheckCount(1)
        }

        val playerCountRow = Table()

        // Wszystkie miejsca są widoczne, ale aktywne jest tylko miejsce nr 2.
        (2..8).forEach { count ->

            val button = TextButton(count.toString(), skin).apply {
                isDisabled = count != 2
            }

            if (count == 2) {
                playerCountGroup.add(button)
            }

            playerCountButtons[count] = button

            button.onChecked { checked ->
                if (checked) {
                    selectedPlayerCount = 2
                    updatePlayerCountSelection()
                }
            }

            playerCountRow.add(button)
                .width(46f)
                .height(40f)
                .pad(4f)
        }

        playersSection.add(playerCountRow)
            .left()
            .row()
        playerCountSummaryLabel = label(
            "",
            color = Color.LIGHT_GRAY
        )

        playersSection.add(playerCountSummaryLabel)
            .left()
            .padTop(6f)

        // Łączymy obie kolumny.
        optionsRow.add(worldSection)
            .width(455f)
            .height(175f)
            .padRight(12f)

        optionsRow.add(playersSection)
            .width(455f)
            .height(175f)

        panel.add(optionsRow)
            .colspan(2)
            .fillX()
            .row()

        // Przywracamy wcześniejsze wybory.
        sizeButtons.getValue(selectedWorldSize).isChecked = true
        playerCountButtons.getValue(selectedPlayerCount).isChecked = true

        updateSizeSelection()
        updatePlayerCountSelection()

        // SEKCJA ZIARNA LOSOWANIA.
        val seedSection = Table(skin).apply {
            background = skin.newDrawable(
                "white",
                Color(0.10f, 0.12f, 0.18f, 1f)
            )
            pad(12f)
        }

        // Nagłówek w osobnym wierszu.
        seedSection.add(
            label("ZIARNO LOSOWANIA", color = Color.CYAN)
        )
            .left()
            .row()

        val seedFieldStyle = TextField.TextFieldStyle().apply {
            font = game.assets.uiFont
            fontColor = Color.WHITE
            focusedFontColor = Color.WHITE
            messageFont = game.assets.uiFont
            messageFontColor = Color.LIGHT_GRAY

            cursor = skin.newDrawable("white", Color.CYAN)

            selection = skin.newDrawable(
                "white",
                Color(0.12f, 0.40f, 0.62f, 0.7f)
            )

            background = skin.newDrawable(
                "white",
                Color(0.06f, 0.08f, 0.13f, 1f)
            )

            focusedBackground = skin.newDrawable(
                "white",
                Color(0.09f, 0.13f, 0.19f, 1f)
            )
        }

        seedField = TextField(
            initialSettings.seed?.toString() ?: "",
            seedFieldStyle
        ).apply {
            messageText = "Pozostaw puste, aby wylosować"
            setMaxLength(19)

            setTextFieldFilter { _, character ->
                character in '0'..'9'
            }
        }

        // Pole tekstowe pod nagłówkiem.
        seedSection.add(seedField)
            .height(44f)
            .expandX()
            .fillX()
            .padTop(8f)

        panel.add(seedSection)
            .colspan(2)
            .fillX()
            .padTop(14f)
            .row()

        // Komunikat o błędach.
        messageLabel = label(
            "",
            color = Color(1f, 0.4f, 0.4f, 1f)
        )

        panel.add(messageLabel)
            .colspan(2)
            .left()
            .padTop(6f)
            .row()

        // Przyciski nawigacji.
        val backButton = TextButton("WRÓĆ", skin).apply {
            pad(10f, 24f, 10f, 24f)

            onClick {
                switchTo(MainMenuScreen(game))
            }
        }

        val continueButton = TextButton(
            "DALEJ",
            skin,
            "primary"
        ).apply {
            pad(10f, 24f, 10f, 24f)

            onClick {
                startConfiguration()
            }
        }

        val buttonsRow = Table()

        buttonsRow.add(backButton)
            .width(170f)
            .height(46f)
            .padRight(12f)

        buttonsRow.add(continueButton)
            .width(190f)
            .height(46f)

        panel.add(buttonsRow)
            .colspan(2)
            .right()
            .padTop(10f)

        root.center()

        root.add(panel)
            .width(990f)
            .pad(14f)
    }

    /**
     * Aktualizuje wygląd wybranego rozmiaru mapy.
     */
    private fun updateSizeSelection() {

        sizeButtons.forEach { (option, button) ->

            val selected = option == selectedWorldSize

            button.style = skin.get(
                if (selected) "primary" else "default",
                TextButton.TextButtonStyle::class.java
            )

            sizeCards.getValue(option).background = skin.newDrawable(
                "white",
                if (selected) {
                    Color(0.09f, 0.22f, 0.30f, 1f)
                } else {
                    Color(0.11f, 0.14f, 0.20f, 1f)
                }
            )
        }

        val option = selectedWorldSize

        sizeSummaryLabel.setText(
            "Wybrano: ${option.displayName.lowercase()} mapa"
        )
    }

    /**
     * Aktualizuje wygląd przycisków liczby graczy.
     */
    private fun updatePlayerCountSelection() {

        playerCountButtons.forEach { (count, button) ->

            button.style = skin.get(
                if (count == selectedPlayerCount) "primary" else "default",
                TextButton.TextButtonStyle::class.java
            )
        }

        playerCountSummaryLabel.setText(
            "Wybrano graczy: $selectedPlayerCount"
        )
    }

    /**
     * Odczytuje konfigurację i przechodzi do wyboru graczy.
     */
    private fun startConfiguration() {

        val seedText = seedField.text.trim()

        val seed = if (seedText.isEmpty()) {
            null
        } else {
            seedText.toLongOrNull()
        }

        if (seedText.isNotEmpty() && seed == null) {
            messageLabel.setText(
                "Podana wartość ziarna jest zbyt duża."
            )
            return
        }

        val settings = NewGameSettings(
            worldSize = selectedWorldSize,
            seed = seed,
            playerCount = selectedPlayerCount
        )

        switchTo(MenuScreen(game, settings))
    }
}



