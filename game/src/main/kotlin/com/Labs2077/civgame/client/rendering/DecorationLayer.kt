package com.Labs2077.civgame.client.rendering

import com.Labs2077.civgame.client.decoration.DecorationId
import com.Labs2077.civgame.client.decoration.PlacedDecoration
import com.badlogic.gdx.math.Vector2
import com.labs2077.civilization.core.models.Hexagon
import kotlin.math.sqrt

/** Dekoracja z pozycją w świecie. */
class DecorationSprite(
    val decorationId: DecorationId,
    val variant: Int,
    val tile: Hexagon,
    val footX: Float,
    val footY: Float
)

/** Określa gdzie umieścić w świecie dekoracje oraz w jakiej kolejności je rysować. */
object DecorationLayer {
    /**
     * Określa rozmiary obszaru kafelka, który jest możliwy do wykorzystania przez dekoracje
     * Na jego podstawie następnie są tworzone konkretne pozycje dekoracji w obrębie kafelka.
     */
    private const val AREA_X = 0.6f
    private const val AREA_Y = 0.45f

    /**
     * Zamienia plan z silnika na listę posortowaną od tyłu do przodu (malejące world Y, remis po X).
     * Liczone raz po wygenerowaniu mapy lub wczytaniu zapisu, nie w render().
     */
    fun build(
        plan: Map<Hexagon, List<PlacedDecoration>>,
        hexSize: Float
    ): List<DecorationSprite> {
        val halfWidth = sqrt(3f) * hexSize / 2f
        val center = Vector2()
        val sprites = ArrayList<DecorationSprite>()
        for ((tile, placed) in plan) {
            HexLayout.axialToWorld(tile, hexSize, center)
            // Tworzymy sprite'y wszystkich dekoracji dla danego kafelka, obliczając ich faktyczne pozycje
            for (p in placed) {
                sprites += DecorationSprite(
                    decorationId = p.decorationId,
                    variant = p.variant,
                    tile = tile,
                    // Licząc przesunięcie od środka kafelka mamy do dyspozycji oczywiście połowę
                    // szerokości i wysokości kafelka dodatkowo przeskalowaną o nasze modyfikatory
                    // obszaru roboczego na dekoracje.
                    footX = center.x + p.offsetX * AREA_X * halfWidth,
                    footY = center.y + p.offsetY * AREA_Y * hexSize
                )
            }
        }
        // Zastosowanie algorytmu malarza. Sortujemy od elementów lężących najwyżej do tych najniżej
        // z uwzględnieniem remisu po X. Dzięki temu dekorajce, które są za innymi zostaną narysowane wcześniej
        // od tych, które są przed nimi. W ten sposób uzyskujemy poprawną kolejność rysowania.
        return sprites.sortedWith(
            compareByDescending<DecorationSprite> { it.footY }.thenBy { it.footX }
        )
    }
}
