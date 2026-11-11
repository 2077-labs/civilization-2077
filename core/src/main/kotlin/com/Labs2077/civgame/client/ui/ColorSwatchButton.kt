package com.Labs2077.civgame.client.ui

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align

/**
 * Przycisk wyboru koloru. Dodany do ButtonGroup działa jak radio button z HTML (zawsze jeden zaznaczony).
 * Zaznaczony kolor lekko się powiększa (animacja Scene2D: scaleTo z interpolacją).
 */
class ColorSwatchButton(skin: Skin, val rgb: Int) : Button(UiSkinFactory.swatchStyle(skin, rgb)) {
    init {
        isTransform = true
        addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val scale = if (isChecked) 1.15f else 1f
                addAction(Actions.scaleTo(scale, scale, 0.12f, Interpolation.smooth))
            }
        })
    }

    // skalowanie względem środka kwadracika, a nie lewego dolnego rogu
    override fun sizeChanged() {
        super.sizeChanged()
        setOrigin(Align.center)
    }
}
