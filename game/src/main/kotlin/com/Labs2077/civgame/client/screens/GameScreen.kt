package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.input.MapInputController
import com.Labs2077.civgame.client.rendering.WorldRenderer
import com.Labs2077.civgame.client.rendering.camera.GameCamera
import com.Labs2077.civgame.client.ui.UiScreen
import com.Labs2077.civgame.client.units.UnitAnimation
import com.Labs2077.civgame.engine.MapTile
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.labs2077.civilization.core.models.City
import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.CivilizationId
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.HexagonGrid
import com.labs2077.civilization.core.models.TerrainDefinitionId
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitDefinitionId

/**
 * MOCK gry na UiScreen: pokazuje, że wypieczone tekstury działają. Dla gracza rysuje jego czołg i miasto
 * w jego kolorze (sprite'y w drawBackground, nazwy graczy i przycisk Menu to widgety Scene2D).
 * Docelowo w tym miejscu stoi HexMapScreen, a ta klasa znika.
 */
class GameScreen(game: CivGame, private val initialCivilizations: List<Civilization>) :
    UiScreen(game) {

    private lateinit var tanks: Map<CivilizationId, Animation<TextureRegion>>

    // Jednostka aktualnie zaznaczona przez gracza.
    private var selectedUnit: Unit? = null

    // Etykieta wyświetlająca informację o zaznaczonej jednostce.
    private lateinit var selectionLabel: Label
    private lateinit var cities: Map<CivilizationId, Animation<TextureRegion>>
    private var time = 0f
    private val worldCamera = GameCamera(
        viewportWidth = 40f, viewportHeight = 22.5f
    )
    private val worldRenderer = WorldRenderer(game.assets)
    private val hexSize = 3f
    private val demoMap = HexagonGrid(
        width = 3, height = 3, tiles = arrayOf(
            MapTile(Hexagon(0, 0), TerrainDefinitionId("crystal_mountains")),
            MapTile(Hexagon(1, 0), TerrainDefinitionId("toxic_swamp")),
            MapTile(Hexagon(2, 0), TerrainDefinitionId("radiation_zone")),

            MapTile(Hexagon(0, 1), TerrainDefinitionId("crystal_mountains")),
            MapTile(Hexagon(1, 1), TerrainDefinitionId("toxic_swamp")),
            MapTile(Hexagon(2, 1), TerrainDefinitionId("radiation_zone")),

            MapTile(Hexagon(-1, 2), TerrainDefinitionId("crystal_mountains")),
            MapTile(Hexagon(0, 2), TerrainDefinitionId("toxic_swamp")),
            MapTile(Hexagon(1, 2), TerrainDefinitionId("radiation_zone"))
        )
    )

    private val demoTiles = demoMap.toList()

    init {
        initialCivilizations.forEachIndexed { index, civilization ->
            val unit = Unit(
                definition = game.unitRuleset.definitions[UnitDefinitionId("tank")]!!,
                civilization = civilization,
                position = demoTiles[index % demoTiles.size].position,
            )
            val city = City(
                civilization = civilization,
                name = "Miasto ${civilization.id}",
                level = game.cityLevelRuleset.levels[0],
                position = demoTiles[(index + 4) % demoTiles.size].position,
            )

            civilization.add(unit)
            civilization.add(city)
        }
    }

    private var civilizations: List<Civilization> = initialCivilizations

    override fun build(root: Table) {
        // animacje tworzymy raz, nie w render()
        tanks = civilizations.associate {
            it.id to game.assets.unitAnimation(
                it.id, UnitDefinitionId("tank"), UnitAnimation.IDLE.id
            )
        }
        cities = civilizations.associate { it.id to game.assets.cityAnimation(it.id, 1) }

        val menuButton = TextButton("Menu", skin).apply {
            pad(10f, 30f, 10f, 30f)
            onClick { switchTo(MenuScreen(game)) }
        }
        root.pad(30f)
            .top()
        root.add(label("GameScreen (mock). Tu będzie HexMapScreen."))
            .expandX()
            .left()
        root.add(menuButton)
            .right()
            .row()
        // Wypełnia resztę ekranu, resztę rysuje drawBackground() (czołgi i miasta).
        root.add()
            .expand()
            .row()

        // nazwy graczy pod czołgami (współrzędne Stage'a, y rośnie w górę)
        civilizations.forEachIndexed { i, c ->
            stage.addActor(label("${c.player.id}").apply {
                setPosition(140f + i * 560f, 270f)
            })
        }
        selectionLabel = label("Nie wybrano jednostki").apply {
            setPosition(30f, 30f)
        }

        stage.addActor(selectionLabel)
    }

    override fun update(delta: Float) {
        time += delta
    }

    override fun onResize(width: Int, height: Int) {
        if (width == 0 || height == 0) return

        worldCamera.viewportWidth = 40f
        worldCamera.viewportHeight = worldCamera.viewportWidth * height.toFloat() / width.toFloat()
        worldCamera.update()
    }

    override fun onResume() {
        worldRenderer.onAssetsRebuilt()

        cities = civilizations.associate {
            it.id to game.assets.cityAnimation(it.id, 1)
        }
    }

    override fun configureInputProcessors() {
        inputs.addProcessor(
            MapInputController(worldCamera) { screenX, screenY ->
                selectUnitAt(screenX, screenY)
            })
    }

    /**
     * Sprawdza, czy na wskazanym przez gracza polu znajduje się jednostka.
     *
     * Najpierw przelicza współrzędne ekranu na współrzędne świata gry,
     * następnie znajduje najbliższy heks i sprawdza obecność jednostki.
     */
    private fun selectUnitAt(screenX: Int, screenY: Int) {

        // Zamieniamy współrzędne ekranu na współrzędne świata gry.
        val worldPosition = worldCamera.unproject(
            Vector3(
                screenX.toFloat(), screenY.toFloat(), 0f
            )
        )

        // Zamieniamy pozycję świata na współrzędne heksagonu.
        val hexCoordinate = worldRenderer.worldToAxial(
            worldPosition.x, worldPosition.y, hexSize
        )

        // Sprawdzamy, czy na wskazanym heksie znajduje się jednostka.
        selectedUnit = civilizations.flatMap { it.units }
            .firstOrNull {
                it.position == hexCoordinate
            }

        // Aktualizujemy informację wyświetlaną na ekranie.
        val unit = selectedUnit
        selectionLabel.setText(
            if (unit != null) {
                "Wybrano jednostke ${unit.id} (gracz ${unit.civilization.id})"
            } else {
                "Brak jednostki na tym polu (${hexCoordinate.q}, ${hexCoordinate.r})"
            }
        )
    }

    override fun drawBackground(delta: Float) {/*
         * Zachowujemy kolejność warstw:
         * najpierw teren, następnie jednostki, a na końcu
         * dodatkowe elementy demonstracyjne.
         *
         * Dzięki temu jednostki są rysowane nad kafelkami terenu.
         */
        worldRenderer.renderTiles(
            tiles = demoMap, camera = worldCamera, hexSize = hexSize
        )
        worldRenderer.renderUnits(
            units = civilizations.flatMap { it.units },
            camera = worldCamera,
            hexSize = hexSize,
            stateTime = time
        )
        // TODO: dodać rysowanie miast
        drawWorld { batch ->
            civilizations.forEachIndexed { i, p ->
                val x = 140f + i * 560f
                val tank = tanks.getValue(p.id)
                    .getKeyFrame(time)
                batch.draw(tank, x, 300f, tank.regionWidth * 3f, tank.regionHeight * 3f)
                val city = cities.getValue(p.id)
                    .getKeyFrame(time)
                batch.draw(city, x, 20f, city.regionWidth * 2f, city.regionHeight * 2f)
            }
        }
    }

    override fun hide() {
//        worldRenderer.dispose()
        super.hide()
    }
}
