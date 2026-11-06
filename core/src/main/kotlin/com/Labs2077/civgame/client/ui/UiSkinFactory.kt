package com.Labs2077.civgame.client.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

/**
 * Buduje Skin w kodzie, bez plików (uiskin.json, atlas): jednokolorowe przyciski z wariantami up/down/over/disabled.
 *
 * Przez to że czcionka jest wspólna (należy do GameAssets), Skin nie zwalnia jej w dispose(). W efekcie można zmieniać ekrany i nie tracić czcionki.
 * Skin.dispose() zwalnia swoje zasoby i zwolniłby też czcionkę używaną przez kolejne ekrany.
 */
object UiSkinFactory {
    private val BLUE = Color(0.10f, 0.45f, 0.80f, 1f)
    private val BLUE_DOWN = Color(0.06f, 0.30f, 0.55f, 1f)
    private val BLUE_OVER = Color(0.20f, 0.55f, 0.90f, 1f)
    private val DISABLED = Color(0.30f, 0.32f, 0.38f, 1f)
    private val DISABLED_TEXT = Color(0.60f, 0.62f, 0.68f, 1f)

    /** Tworzy Skin zbudowany w kodzie, z podaną czcionką. */
    fun create(font: BitmapFont): Skin {
        val skin = Skin()

        // jeden piksel w bieli: wszystkie potrzebne ramki to ten sam region barwiony na potrzebny kolor
        val pixel = Texture(Pixmap(1, 1, Pixmap.Format.RGBA8888).apply { setColor(Color.WHITE); fill() })
        skin.add("pixel", pixel)
        skin.add("white", TextureRegionDrawable(TextureRegion(pixel)), Drawable::class.java)

        skin.add("default", Label.LabelStyle(font, Color.WHITE))
        skin.add("default", TextButton.TextButtonStyle().apply {
            this.font = font
            up = skin.newDrawable("white", BLUE)
            down = skin.newDrawable("white", BLUE_DOWN)
            over = skin.newDrawable("white", BLUE_OVER)
            disabled = skin.newDrawable("white", DISABLED)
            fontColor = Color.WHITE
            disabledFontColor = DISABLED_TEXT
        })
        return skin
    }

    /** Styl kwadracika z kolorem (0xRRGGBB): zaznaczony ma białą ramkę. Tworzony raz na kolor i zapamiętywany w skinie. */
    fun swatchStyle(skin: Skin, rgb: Int): Button.ButtonStyle {
        // Utworzone style zapisujemy w skinie pod nazwą "swatch-0xRRGGBB", żeby nie tworzyć duplikatów.
        val name = "swatch-$rgb"
        if (skin.has(name, Button.ButtonStyle::class.java)) return skin.get(name, Button.ButtonStyle::class.java)

        val color = Color((rgb shl 8) or 0xFF)
        val checkedTexture = borderedTexture(color)
        skin.add("$name-checked", checkedTexture)
        val style = Button.ButtonStyle().apply {
            up = skin.newDrawable("white", color)
            over = skin.newDrawable("white", color.cpy().lerp(Color.WHITE, 0.25f))
            // Jedynie zaznaczony wariant ma białą ramkę (borderedTexture). Warianty up/over są jednokolorowe.
            checked = TextureRegionDrawable(TextureRegion(checkedTexture))
        }
        skin.add(name, style)
        return style
    }

    /** Tworzy kwadracik z kolorem i białą ramką. */
    private fun borderedTexture(color: Color): Texture {
        val pixmap = Pixmap(16, 16, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE); pixmap.fill()
        pixmap.setColor(color); pixmap.fillRectangle(3, 3, 10, 10)
        // Zwracamy jedynie texturę, robocza pixmapa z CPU zostaje zwolniona.
        return Texture(pixmap).also { pixmap.dispose() }
    }
}
