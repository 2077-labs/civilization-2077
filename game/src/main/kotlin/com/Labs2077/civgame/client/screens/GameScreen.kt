package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.Labs2077.civgame.client.decoration.DecorationPlanner
import com.Labs2077.civgame.client.input.MapInputController
import com.Labs2077.civgame.client.rendering.CitySprite
import com.Labs2077.civgame.client.rendering.DecorationLayer
import com.Labs2077.civgame.client.rendering.HexLayout
import com.Labs2077.civgame.client.rendering.UnitSprite
import com.Labs2077.civgame.client.rendering.WorldRenderer
import com.Labs2077.civgame.client.rendering.camera.GameCamera
import com.Labs2077.civgame.client.ui.UiScreen
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.labs2077.civilization.core.map_generation.ClusterMapGenerator
import com.labs2077.civilization.core.map_generation.MapGenerationConfig
import com.labs2077.civilization.core.map_generation.MapGenerator
import com.labs2077.civilization.core.models.City
import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.CivilizationId
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.HexagonGrid
import com.labs2077.civilization.core.models.Terrain
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitDefinitionId
import kotlin.math.max
import kotlin.random.Random
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * Stan pomocniczy jednostki spacerującej po mapie demonstracyjnej (wyłącznie na potrzeby demo, bez silnika).
 *
 * Przechowuje aktualną ścieżkę po siatce heksagonalnej oraz postęp interpolacji między kafelkami,
 * co pozwala przetestować animację ruchu oraz sortowanie głębokości względem dekoracji.
 *
 * @property civilizationId identyfikator cywilizacji.
 * @property unitTypeId identyfikator definicji jednostki z [CivGame.unitRuleset].
 * @property rng niezależny generator losowy dla tej jednostki (wybór celu i czasu postoju).
 */
private class Walker(
    val civilizationId: CivilizationId,
    val unitTypeId: UnitDefinitionId,
    start: Hexagon,
    val rng: Random
) {
    var tile: Hexagon = start
    var path: List<Hexagon> = emptyList()

    /** Indeks bieżącego odcinka na liście [path]. */
    var step = 0

    /** Postęp interpolacji między kafelkiem [step] a [step] + 1 w zakresie 0..1. */
    var t = 0f

    /** Pozostały czas postoju (w sekundach) przed wylosowaniem kolejnej trasy. */
    var wait = rng.nextFloat() * 2f

    /** Czy sprite jednostki powinien być odbity w poziomie (kierunek w lewo). */
    var flipX = false
    val isMoving: Boolean get() = path.isNotEmpty()
}

/**
 * Prezentuje działanie renderowania: wyświetla siatkę heksagonalną z losowymi skupiskami terenów,
 * warstwę dekoracji generowaną przez [DecorationPlanner] i [DecorationLayer], miasta cywilizacji
 * oraz jednostki poruszające się po mapie z uwzględnieniem przesłaniania przez dekoracje.
 *
 *
 * TODO: Docelowo ekran zostanie zastąpiony właściwym ekranem rozgrywki zasilanym stanem z silnika gry.
 */
class GameScreen(game: CivGame, initialCivilizations: List<Civilization>) : UiScreen(game) {
    private companion object {
        const val MAP_WIDTH = 14
        const val MAP_HEIGHT = 10
        const val MAP_SEED = 77L
        const val WALKER_COUNT = 6

        /** Prędkość poruszania się jednostek demo wyrażona w kafelkach na sekundę. */
        const val WALK_SPEED = 1.4f

        /** Maksymalna liczba kroków w pojedynczej losowanej ścieżce. */
        const val MAX_PATH_STEPS = 8
    }

    /** Całkowity czas działania ekranu w sekundach — służy do wybierania klatek animacji. */
    private var time = 0f

    /** Promień heksagonu (odległość od środka do wierzchołka) w jednostkach świata gry. */
    private val hexSize = 3f

    private val worldCamera = GameCamera(viewportWidth = 40f, viewportHeight = 22.5f)
    private val worldRenderer = WorldRenderer(game.assets)

    /** TODO: Domyślnie oczywiście mapa generowana w silniku i jedynie wykorzystywana w ekranie gry */
    private val mapGenerator: MapGenerator = ClusterMapGenerator()
    private val worldMap: HexagonGrid<Terrain> = mapGenerator.generate(
        config = MapGenerationConfig(MAP_WIDTH, MAP_HEIGHT, MAP_SEED),
        terrains = game.terrainRuleset
    )
    private val worldTiles = worldMap.toList()

    /** Lista wszystkich przechodnich współrzędnych mapy (używana do rozmieszczania miast i losowania tras). */
    private val passableList: List<Hexagon> =
        worldTiles.filter { it.definition.passable }
            .map { it.position }
            .also { check(it.isNotEmpty()) { "Demo mapa nie ma żadnego przechodniego kafelka" } }

    /** Zbiór przechodnich współrzędnych dla szybkiego sprawdzania podczas walidacji ścieżki. */
    private val passable: Set<Hexagon> = passableList.toSet()

    // Jednostka aktualnie zaznaczona przez gracza.
    private var selectedUnit: Unit? = null

    // Etykieta wyświetlająca informację o zaznaczonej jednostce.
    private lateinit var selectionLabel: Label

    /*
     * Miasta startowe cywilizacji: pierwsza cywilizacja trafia na początek listy przechodnich
     * kafelków, a ostatnia na jej koniec, aby rozsunąć miasta na mapie.
     */
    private val civilizations: List<Civilization> =
        initialCivilizations.onEachIndexed { index, civilization ->
            val position =
                passableList[index * (passableList.size - 1) / max(
                    1,
                    initialCivilizations.size - 1
                )]
            val city = City(
                civilization = civilization,
                name = "Miasto ${index + 1}",
                level = game.cityLevelRuleset.initial,
                position = position
            )
            civilization.add(city)
        }

    /** Statyczne sprite'y miast przekazywane do [WorldRenderer.renderWorldObjects]. */
    private val citySprites = civilizations.flatMap { civ ->
        civ.cities.map { CitySprite(it) }
    }

    /** Jednostki demonstracyjne rozdzielone cyklicznie pomiędzy cywilizacje i mobilne typy z rulesetu. */
    private val walkers: List<Walker> = run {
        val rng = Random(MAP_SEED + 1)
        val types = game.unitRuleset.definitions.values.filter { it.movement > 0 }
        List(WALKER_COUNT) { i ->
            Walker(
                civilizationId = civilizations[i % civilizations.size].id,
                unitTypeId = types[i % types.size].id,
                start = passableList[rng.nextInt(passableList.size)],
                rng = Random(rng.nextLong())
            )
        }
    }

    // TODO: docelowo ziarno pochodzi z ustawień nowej gry lub wczytanego zapisu
    private var mapSeed = 2077L

    /** Posortowana warstwa dekoracji; przebudowywana wyłącznie przy zmianie mapy lub seeda, nie w render(). */
    private var decorationSprites = buildDecorations()

    /** Określa, czy dekoracje stojące przed jednostką mają być przygaszane (półprzezroczyste). */
    private var fadeOccluders = true

    // Robocze wektory wielokrotnego użytku, żeby nie alokować co klatke.
    private val tmpA = Vector2()
    private val tmpB = Vector2()

    private lateinit var fadeButton: TextButton

    init {
        // Ustawiamy kamerę na geometrycznym środku wygenerowanej mapy.
        val center = Vector2()
        var sumX = 0f
        var sumY = 0f
        worldTiles.forEach {
            HexLayout.axialToWorld(it.position, hexSize, center)
            sumX += center.x
            sumY += center.y
        }
        worldCamera.moveTo(sumX / worldTiles.size, sumY / worldTiles.size)
        worldCamera.changeZoom(1.4f)
    }

    /**
     * Planuje rozmieszczenie dekoracji dla aktualnej mapy i zamienia plan na listę posortowaną
     * algorytmem malarza (od tyłu do przodu). Pola zajęte przez miasta są pomijane.
     */
    private fun buildDecorations() = DecorationLayer.build(
        plan = DecorationPlanner.planMap(
            tiles = worldTiles,
            ruleset = game.decorationRuleset,
            mapSeed = mapSeed,
            suppressed = citySprites.map { it.tile }
                .toSet()
        ),
        hexSize = hexSize
    )

    /**
     * Buduje losową trasę po sąsiednich przechodnich kafelkach (bez natychmiastowego zawracania).
     * Jeśli jednostka jest otoczona nieprzechodnim terenem, chwilę odczekuje.
     */
    private fun pickPath(w: Walker) {
        val steps = w.rng.nextInt(2, MAX_PATH_STEPS + 1)
        val path = mutableListOf(w.tile)

        repeat(steps) {
            val current = path.last()
            val previous = path.getOrNull(path.size - 2)
            val candidates = current.neighbors()
                .filter { it in passable && it != previous }
            if (candidates.isEmpty()) return@repeat
            path += candidates.random(w.rng)
        }

        if (path.size >= 2) {
            w.path = path
            w.step = 0
            w.t = 0f
        } else {
            w.wait = 1f
        }
    }

    /**
     * Aktualizuje stany ruchu i postoju wszystkich jednostek demonstracyjnych.
     *
     * @param delta czas od poprzedniej klatki w sekundach
     */
    private fun updateWalkers(delta: Float) {
        for (w in walkers) {
            if (!w.isMoving) {
                w.wait -= delta
                if (w.wait <= 0f) pickPath(w)
                continue
            }
            w.t += delta * WALK_SPEED
            while (w.t >= 1f && w.isMoving) {
                w.t -= 1f
                w.step++
                if (w.step >= w.path.size - 1) {
                    w.tile = w.path.last()
                    w.path = emptyList()
                    w.step = 0
                    w.t = 0f
                    w.wait = 0.5f + w.rng.nextFloat() * 1.5f
                }
            }
        }
    }

    /**
     * Buduje listę [UnitSprite] w bieżących pozycjach świata na podstawie interpolacji
     * między środkami sąsiednich kafelków (docelowo pozycję powinna dostarczyć warstwa animacji ruchu silnika).
     */
    private fun unitSprites(): List<UnitSprite> = walkers.map { w ->
        val from = w.path.getOrNull(w.step) ?: w.tile
        val to = w.path.getOrNull(w.step + 1) ?: from
        HexLayout.axialToWorld(from, hexSize, tmpA)
        HexLayout.axialToWorld(to, hexSize, tmpB)
        val dx = tmpB.x - tmpA.x
        // Obracamy sprite w stronę ruchu, a przy ruchu pionowym zachowujemy ostatni zwrot.
        if (dx > 0.01f) w.flipX = false else if (dx < -0.01f) w.flipX = true
        UnitSprite(
            civilizationId = w.civilizationId,
            unitTypeId = w.unitTypeId,
            x = tmpA.x + dx * w.t,
            y = tmpA.y + (tmpB.y - tmpA.y) * w.t,
            moving = w.isMoving,
            flipX = w.flipX
        )
    }

    /** Synchronizuje etykietę przycisku przesłaniania z aktualną wartością flagi [fadeOccluders]. */
    private fun refreshButtons() {
        fadeButton.setText("Półprzezroczyste przeszkody: " + if (fadeOccluders) "TAK" else "NIE")
    }

    /** Buduje górny pasek narzędziowy Scene2D nad obszarem renderowanej mapy. */
    override fun build(root: Table) {
        val menuButton = TextButton("Menu", skin).apply {
            pad(10f, 30f, 10f, 30f)
            onClick { switchTo(MenuScreen(game)) }
        }
        fadeButton = TextButton("", skin).apply {
            pad(10f, 20f, 10f, 20f)
            onClick {
                fadeOccluders = !fadeOccluders
                refreshButtons()
            }
        }
        val seedButton = TextButton("Nowy seed dekoracji", skin).apply {
            pad(10f, 20f, 10f, 20f)
            onClick {
                mapSeed = Random.nextLong()
                decorationSprites = buildDecorations()
            }
        }
        selectionLabel = label("Nie wybrano jednostki").apply {
            setPosition(30f, 30f)
        }
        refreshButtons()

        val buttons = Table().apply {
            add(fadeButton).padRight(10f)
            add(seedButton).padRight(10f)
            add(menuButton)
        }


        root.pad(30f)
            .top()
        root.add(buttons)
            .colspan(3)
            .left()
            .padTop(10f)
            .row()
        root.add()
            .expand()
            .row()

        stage.addActor(selectionLabel)
    }

    override fun update(delta: Float) {
        time += delta
        updateWalkers(delta)
    }

    override fun onResize(width: Int, height: Int) {
        if (width == 0 || height == 0) return
        // Dopasowujemy wysokość widoku kamery świata do proporcji okna, utrzymując stałą szerokość logiczną.
        worldCamera.viewportWidth = 40f
        worldCamera.viewportHeight = worldCamera.viewportWidth * height.toFloat() / width.toFloat()
        worldCamera.update()
    }

    override fun onResume() {
        // Po utracie kontekstu OpenGL atlasy są odbudowywane, więc czyścimy cache animacji w rendererze.
        worldRenderer.onAssetsRebuilt()
    }

    override fun configureInputProcessors() {
        // Stage jest rejestrowany w UiScreen jako pierwszy, więc UI przechwytuje kliknięcia przed gestami mapy.
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

    override fun drawBackground(delta: Float) {
        /*
         * Zachowujemy kolejność warstw:
         * najpierw kafelki terenu, a następnie obiekty świata (miasta, dekoracje i jednostki)
         * scalane w jednym przebiegu z sortowaniem po osi Y.
         */
        worldRenderer.renderTiles(tiles = worldMap, camera = worldCamera, hexSize = hexSize)
        worldRenderer.renderWorldObjects(
            cities = citySprites,
            decorations = decorationSprites,
            units = unitSprites(),
            camera = worldCamera,
            hexSize = hexSize,
            stateTime = time,
            fadeOccluders = fadeOccluders
        )
    }

    override fun hide() {
        worldRenderer.dispose()
        super.hide()
    }
}
