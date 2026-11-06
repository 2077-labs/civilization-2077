package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.ui.UiScreen
import com.Labs2077.civgame.client.units.UnitAnimation
import com.Labs2077.civgame.engine.PlayerSetup
import com.Labs2077.civgame.engine.rules.UnitId
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

/**
 * MOCK gry na UiScreen: pokazuje, że wypieczone tekstury działają. Dla gracza rysuje jego czołg i miasto
 * w jego kolorze (sprite'y w drawBackground, nazwy graczy i przycisk Menu to widgety Scene2D).
 * Docelowo w tym miejscu stoi HexMapScreen, a ta klasa znika.
 */
class GameScreen(game: CivGame, private val players: List<PlayerSetup>) : UiScreen(game) {
    private lateinit var tanks: Map<Int, Animation<TextureRegion>>
    private lateinit var cities: Map<Int, Animation<TextureRegion>>
    private var time = 0f

    override fun build(root: Table) {
        // animacje tworzymy raz, nie w render()
        tanks = players.associate { it.id to game.assets.unitAnimation(it.id, UnitId("tank"), UnitAnimation.IDLE.id) }
        cities = players.associate { it.id to game.assets.cityAnimation(it.id, 1) }

        val menuButton = TextButton("Menu", skin).apply {
            pad(10f, 30f, 10f, 30f)
            onClick { switchTo(MenuScreen(game)) }
        }
        root.pad(30f).top()
        root.add(label("GameScreen (mock). Tu będzie HexMapScreen.")).expandX().left()
        root.add(menuButton).right().row()
        // Wypełnia resztę ekranu, resztę rysuje drawBackground() (czołgi i miasta).
        root.add().expand().row()

        // nazwy graczy pod czołgami (współrzędne Stage'a, y rośnie w górę)
        players.forEachIndexed { i, p ->
            stage.addActor(label(p.name).apply { setPosition(140f + i * 560f, 270f) })
        }
    }

    override fun update(delta: Float) {
        time += delta
    }

    override fun drawBackground(delta: Float) = drawWorld { batch ->
        players.forEachIndexed { i, p ->
            val x = 140f + i * 560f
            val tank = tanks.getValue(p.id).getKeyFrame(time)
            batch.draw(tank, x, 300f, tank.regionWidth * 3f, tank.regionHeight * 3f)
            val city = cities.getValue(p.id).getKeyFrame(time)
            batch.draw(city, x, 20f, city.regionWidth * 2f, city.regionHeight * 2f)
        }
    }
}
