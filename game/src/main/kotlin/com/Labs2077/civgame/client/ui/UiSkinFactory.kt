
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
    private val PRIMARY = Color(0.12f, 0.40f, 0.62f, 1f)
    private val PRIMARY_DOWN = Color(0.08f, 0.30f, 0.48f, 1f)
    private val PRIMARY_OVER = Color(0.17f, 0.48f, 0.70f, 1f)

    private val SECONDARY = Color(0.16f, 0.19f, 0.25f, 1f)
    private val SECONDARY_DOWN = Color(0.12f, 0.15f, 0.21f, 1f)
    private val SECONDARY_OVER = Color(0.22f, 0.27f, 0.35f, 1f)

    private val DISABLED = Color(0.25f, 0.27f, 0.31f, 1f)
    private val DISABLED_TEXT = Color(0.60f, 0.62f, 0.66f, 1f)

    /** Tworzy Skin zbudowany w kodzie, z podaną czcionką. */
    fun create(font: BitmapFont): Skin {
        val skin = Skin()

        // jeden piksel w bieli: wszystkie potrzebne ramki to ten sam region barwiony na potrzebny kolor
        val pixel =
            Texture(Pixmap(1, 1, Pixmap.Format.RGBA8888).apply { setColor(Color.WHITE); fill() })
        skin.add("pixel", pixel)
        skin.add("white", TextureRegionDrawable(TextureRegion(pixel)), Drawable::class.java)

        skin.add("default", Label.LabelStyle(font, Color.WHITE))

        skin.add(
            "default",
            buttonStyle(
                skin,
                font,
                SECONDARY,
                SECONDARY_DOWN,
                SECONDARY_OVER
            )
        )

        skin.add(
            "primary",
            buttonStyle(
                skin,
                font,
                PRIMARY,
                PRIMARY_DOWN,
                PRIMARY_OVER
            )
        )

        return skin
    }

    private fun buttonStyle(
        skin: Skin,
        font: BitmapFont,
        upColor: Color,
        downColor: Color,
        overColor: Color
    ): TextButton.TextButtonStyle {
        return TextButton.TextButtonStyle().apply {
            this.font = font
            up = skin.newDrawable("white", upColor)
            down = skin.newDrawable("white", downColor)
            over = skin.newDrawable("white", overColor)
            disabled = skin.newDrawable("white", DISABLED)

            fontColor = Color.WHITE
            disabledFontColor = DISABLED_TEXT
        }
    }

    /** Styl kwadracika z kolorem (0xRRGGBB): zaznaczony ma białą ramkę. Tworzony raz na kolor i zapamiętywany w skinie. */
    fun swatchStyle(skin: Skin, rgb: Int): Button.ButtonStyle {
        // Utworzone style zapisujemy w skinie pod nazwą "swatch-0xRRGGBB", żeby nie tworzyć duplikatów.
        val name = "swatch-$rgb"
        if (skin.has(name, Button.ButtonStyle::class.java)) return skin.get(
            name,
            Button.ButtonStyle::class.java
        )

        val color = Color((rgb shl 8) or 0xFF)
        val checkedTexture = borderedTexture(color)
        skin.add("$name-checked", checkedTexture)
        val style = Button.ButtonStyle()
            .apply {
                up = skin.newDrawable("white", color)
                over = skin.newDrawable(
                    "white",
                    color.cpy()
                        .lerp(Color.WHITE, 0.25f)
                )
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
