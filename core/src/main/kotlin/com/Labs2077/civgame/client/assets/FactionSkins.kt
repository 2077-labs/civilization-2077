package com.Labs2077.civgame.client.assets

import com.Labs2077.civgame.engine.PlayerSetup
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable

// Dla każdego gracza przetrzymywany jest osobny wypiekany atlas jednostek (i miast). Jest to realnie kopia bazowego
// atlasu, w której zarezerwowane kolory magenty są zamieniane na kolor gracza:
//   #FF00FF (255, 0, 255)  -> kolor gracza
//   #AA00AA (170, 0, 170)  -> ciemniejszy odcień koloru gracza (cieniowanie, np. fałdy flagi)
// Tych dwóch kolorów nie wolno używać w grafikach do niczego innego.
class FactionSkins(private val packFile: FileHandle) : Disposable {
    private companion object {
        // getPixel zwraca RGBA8888, więc nieprzezroczysta magenta to 0xFF00FFFF (R=FF G=00 B=FF A=FF).
        val MAGENTA: Int = 0xFF00FFFF.toInt()
        val MAGENTA_DARK: Int = 0xAA00AAFF.toInt()
    }

    private val atlases = HashMap<Int, TextureAtlas>()
    private var players: List<PlayerSetup> = emptyList()

    fun bake(players: List<PlayerSetup>) {
        dispose()
        this.players = players
        for (p in players) atlases[p.id] = bakeAtlas(p.colorRgb)
    }

    fun rebuild() = bake(players)

    // Ciemniejszy odcień koloru gracza (np. fałdy flagi)
    private fun shade(rgb: Int): Int {
        val r = ((rgb shr 16) and 0xFF) * 2 / 3
        val g = ((rgb shr 8) and 0xFF) * 2 / 3
        val b = (rgb and 0xFF) * 2 / 3
        return (r shl 24) or (g shl 16) or (b shl 8) or 0xFF
    }

    private fun bakeAtlas(rgb: Int): TextureAtlas {
        val main = (rgb shl 8) or 0xFF
        val dark = shade(rgb)
        val data = TextureAtlas.TextureAtlasData(packFile, packFile.parent(), false)
        for (page in data.pages) {
            val src = Pixmap(page.textureFile)
            val out = Pixmap(src.width, src.height, Pixmap.Format.RGBA8888)
            out.blending = Pixmap.Blending.None

            for (y in 0 until src.height) for (x in 0 until src.width) {
                val pixel = src.getPixel(x, y)
                out.drawPixel(x, y, when (pixel) {
                    MAGENTA -> main
                    MAGENTA_DARK -> dark
                    else -> pixel
                })
            }
            page.texture = Texture(out).apply { setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest) }
            src.dispose()
            out.dispose()
        }
        return TextureAtlas(data)
    }

    fun frames(playerId: Int, name: String): Array<TextureAtlas.AtlasRegion> =
        atlases.getValue(playerId).findRegions(name)

    fun hasFrames(name: String): Boolean =
        atlases.values.firstOrNull()?.findRegions(name)?.let { it.size > 0 } ?: false

    override fun dispose() {
        atlases.values.forEach { it.dispose() }
        atlases.clear()
    }
}
