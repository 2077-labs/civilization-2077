package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.input.MapInputController
import com.Labs2077.civgame.client.ui.UiScreen
import com.Labs2077.civgame.client.units.UnitAnimation
import com.Labs2077.civgame.engine.PlayerSetup
import com.Labs2077.civgame.engine.rules.UnitId
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.Labs2077.civgame.client.rendering.WorldRenderer
import com.Labs2077.civgame.client.rendering.camera.GameCamera
import com.Labs2077.civgame.engine.map.HexCoordinate
import com.Labs2077.civgame.engine.map.HexGrid
import com.Labs2077.civgame.engine.map.MapTile
import com.Labs2077.civgame.engine.rules.TerrainId
import com.Labs2077.civgame.engine.units.UnitInstance
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2

/**
 * MOCK gry na UiScreen: pokazuje, że wypieczone tekstury działają. Dla gracza rysuje jego czołg i miasto
 * w jego kolorze (sprite'y w drawBackground, nazwy graczy i przycisk Menu to widgety Scene2D).
 * Docelowo w tym miejscu stoi HexMapScreen, a ta klasa znika.
 */
class GameScreen(game: CivGame, private val players: List<PlayerSetup>) : UiScreen(game) {
    private lateinit var cities: Map<Int, Animation<TextureRegion>>
    private var time = 0f
    private val worldCamera = GameCamera(
        viewportWidth = 40f,
        viewportHeight = 22.5f
    )
    private val worldRenderer = WorldRenderer(game.assets)
    private val hexSize = 3f
    private val demoMap = HexGrid<MapTile>(
        width = 3,
        height = 3,
        tiles = arrayOf(
            MapTile(HexCoordinate(0, 0), TerrainId("crystal_mountains")),
            MapTile(HexCoordinate(1, 0), TerrainId("toxic_swamp")),
            MapTile(HexCoordinate(2, 0), TerrainId("radiation_zone")),

            MapTile(HexCoordinate(0, 1), TerrainId("crystal_mountains")),
            MapTile(HexCoordinate(1, 1), TerrainId("toxic_swamp")),
            MapTile(HexCoordinate(2, 1), TerrainId("radiation_zone")),

            MapTile(HexCoordinate(-1, 2), TerrainId("crystal_mountains")),
            MapTile(HexCoordinate(0, 2), TerrainId("toxic_swamp")),
            MapTile(HexCoordinate(1, 2), TerrainId("radiation_zone"))
        )
    )

    private val demoTiles = demoMap.toList()
    private val demoUnits: List<UnitInstance> by lazy {
        players.mapIndexed { index, player ->
            UnitInstance(
                instanceId = index.toLong(),
                definitionId = UnitId("tank"),
                ownerId = player.id,
                position = demoTiles[index % demoTiles.size].position,
                currentHp = 100,
                movementRemaining = 2
            )
        }
    }
    override fun build(root: Table) {
        // animacje tworzymy raz, nie w render()
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

    override fun onResize(width: Int, height: Int) {
        if(width == 0 || height == 0) return

        worldCamera.viewportWidth = 40f
        worldCamera.viewportHeight =
            worldCamera.viewportWidth * height.toFloat() / width.toFloat()
        worldCamera.update()
    }
    override fun onResume() {
        worldRenderer.onAssetsRebuilt()

        cities = players.associate {
            it.id to game.assets.cityAnimation(it.id, 1)
        }
    }
    override fun configureInputProcessors() {
        inputs.addProcessor(MapInputController(worldCamera))
    }
    override fun drawBackground(delta: Float) {
        /*
         * Zachowujemy kolejność warstw:
         * najpierw teren, następnie jednostki, a na końcu
         * dodatkowe elementy demonstracyjne.
         *
         * Dzięki temu jednostki są rysowane nad kafelkami terenu.
         */
        worldRenderer.renderTiles(
            tiles = demoMap,
            camera = worldCamera,
            hexSize = hexSize
        )
        worldRenderer.renderUnits(
            units = demoUnits,
            camera = worldCamera,
            hexSize = hexSize,
            stateTime = time
        )
        drawWorld { batch ->
            players.forEachIndexed { i, p ->
                val x = 140f + i * 560f
                val city = cities.getValue(p.id).getKeyFrame(time)
                batch.draw(city, x, 20f, city.regionWidth * 2f, city.regionHeight * 2f)
            }
        }
    }

    override fun hide() {
        worldRenderer.dispose()
        super.hide()
    }
}
