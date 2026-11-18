package com.Labs2077.civgame.client.screens

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessFiles
import org.junit.BeforeClass

class MenuColorSelectionScene2DTest {

    /**
     * Tworzy grupę przycisków kolorów dla jednego gracza.
     *
     * Grupa zachowuje się jak radio button:
     * można zaznaczyć tylko jeden kolor.
     */
    private fun createColorButtons(
        selection: MenuColorSelection,
        player: Int
    ): List<Button> {

        val group = ButtonGroup<Button>().apply {
            setMinCheckCount(1)
            setMaxCheckCount(1)
        }

        val buttons = selection.palette.map {
            Button(Button.ButtonStyle())
        }

        buttons.forEachIndexed { index, button ->
            group.add(button)

            button.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    if (button.isChecked) {
                        selection.selectColor(player, index)
                    }
                }
            })
        }

        buttons[selection.selectedColorIndex(player)].isChecked = true

        return buttons
    }

    @Test
    fun selectingAnotherButtonUnchecksPreviousButton() {
        val selection = MenuColorSelection()
        val buttons = createColorButtons(selection, 0)

        buttons[2].isChecked = true

        assertTrue(buttons[2].isChecked)
        assertFalse(buttons[0].isChecked)
        assertEquals(2, selection.selectedColorIndex(0))
    }

    @Test
    fun selectingSameColorForBothPlayersPreventsStarting() {
        val selection = MenuColorSelection()

        createColorButtons(selection, 0)
        val secondPlayerButtons = createColorButtons(selection, 1)

        secondPlayerButtons[0].isChecked = true

        assertFalse(selection.canStart)

        secondPlayerButtons[2].isChecked = true

        assertTrue(selection.canStart)
    }

    @Test
    fun playersHaveIndependentButtonGroups() {
        val selection = MenuColorSelection()

        val firstPlayerButtons = createColorButtons(selection, 0)
        val secondPlayerButtons = createColorButtons(selection, 1)

        firstPlayerButtons[1].isChecked = true

        assertEquals(1, selection.selectedColorIndex(0))
        assertEquals(3, selection.selectedColorIndex(1))

        assertTrue(secondPlayerButtons[3].isChecked)
        assertFalse(firstPlayerButtons[0].isChecked)
    }
    companion object {
        @BeforeClass
        @JvmStatic
        fun initializeLibGdx() {
            Gdx.files = HeadlessFiles()
        }
    }
}