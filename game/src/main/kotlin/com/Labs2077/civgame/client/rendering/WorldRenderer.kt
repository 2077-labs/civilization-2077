package com.Labs2077.civgame.client.rendering

import com.Labs2077.civgame.client.assets.AssetProvider
import com.Labs2077.civgame.client.decoration.DecorationId
import com.Labs2077.civgame.client.rendering.camera.GameCamera
import com.Labs2077.civgame.client.units.UnitAnimation
import com.badlogic.gdx.graphics.Color
import com.Labs2077.civgame.engine.MapTile
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.labs2077.civilization.core.models.CivilizationId
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.Terrain
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitDefinitionId
import kotlin.math.sqrt

class WorldRenderer(
    private val assets: AssetProvider
) {
    private val batch = PolygonSpriteBatch()
    private val worldPosition = Vector2()

    /*
     * Cache animacji jednostek.
     *
     * Klucz składa się z ID właściciela, ID typu jednostki oraz nazwy animacji (np. "idle", "move").
     * dzięki czemu każdy gracz może korzystać z animacji
     * przygotowanej dla swojej frakcji.
     *
     * Animacje są pobierane z GameAssets tylko wtedy,
     * gdy nie ma ich jeszcze w cache.
     */
    private val stateAnimations =
        mutableMapOf<Triple<CivilizationId, UnitDefinitionId, UnitAnimation>, Animation<TextureRegion>>()

    // analogiczny cache wariantów dekoracji: id -> klatki z atlasu
    private val decorationFrames = mutableMapOf<DecorationId, Array<out TextureRegion>>()

    // analogiczny cache animacji miast: (właściciel, poziom) -> animacja
    private val cityAnimations = mutableMapOf<Pair<CivilizationId, Int>, Animation<TextureRegion>>()

    private companion object {
        const val TILE_TEXTURE_WIDTH_PX = 128f

        /** Stopy jednostki leżą tyle (w hexSize) poniżej środka kafelka. Do strojenia. */
        const val UNIT_FOOT_OFFSET = 0.35f
    }

    /**
     * Rysuje na ekranie wszystkie widoczne kafelki
     * @param tiles zbiór kafelków
     * @param camera kamera, która wyświetli widoczne kafelki
     * @param hexSize promień heksagonu (odległość od środka do wierzchołka)
     *                wyrażony w jednostkach świata gry.
     */
    fun renderTiles(
        tiles: Iterable<Terrain>, camera: GameCamera, hexSize: Float
    ) {
        // Promień hexa nie może być mniejszy lub równy 0
        require(hexSize > 0f)

        val sqrtOfThree = sqrt(3f)
        val tileWidth = sqrtOfThree * hexSize
        val tileHeight = 2f * hexSize

        /*
            Oblicza połowę szerokości i wysokości obszaru,
            który aktualnie obejmuje kamera, uwzględniając jej przybliżenie
         */
        val halfWidth = camera.viewportWidth * camera.zoom / 2f
        val halfHeight = camera.viewportHeight * camera.zoom / 2f

        /*
            Wyznacza dokładne współrzędne czterech krawędzi ekranu
            w świecie gry (od środka kamery odejmuje lub dodaje połowę jej wymiarów)
         */
        val cameraLeft = camera.position.x - halfWidth
        val cameraRight = camera.position.x + halfWidth

        val cameraBottom = camera.position.y - halfHeight
        val cameraTop = camera.position.y + halfHeight

        /*
        Przekazuje do SpriteBatch macierz kamery,
        dzięki której współrzędne obiektów podawane w świecie gry
        są poprawnie przekształcane do przestrzeni widoku.
        */
        batch.projectionMatrix = camera.combined
        batch.begin()

        try {
            for (tile in tiles) {
                // Zamiana współrzędnych heksa z axial na pozycję świata
                val center = axialToWorld(tile.position, hexSize, worldPosition)

                // Pomijanie niewidocznych kafelków
                if (!isVisible(
                        center.x,
                        center.y,
                        tileWidth,
                        tileHeight,
                        cameraLeft,
                        cameraRight,
                        cameraBottom,
                        cameraTop
                    )
                ) {
                    continue
                }

                // Pobieranie odpowiedniej grafiki dla danego terenu
                val region = assets.terrain(tile.definition.id)
                // Wyznaczenie skali wysokości regionu tekstury do wysokości kafelka w świecie gry
                val scale = tileHeight / region.regionHeight
                // Na podstawie tej skali wiemy, jak duży obszar w świecie gry zajmie grafika
                val drawW = region.regionWidth * scale
                val drawH = region.regionHeight * scale

                // Rysowanie grafiki na ekranie
                batch.draw(
                    HexMask.of(region),
                    center.x - drawW / 2f,
                    center.y - drawH / 2f,
                    drawW,
                    drawH
                )
            }
        } finally {
            // Kończy rozpoczętą partię rysowania.
            // SpriteBatch opróżnia bufor zgromadzonych operacji rysowania
            // i przekazuje je do przetworzenia przez kartę graficzną.
            batch.end()
        }
    }

    /**
     * Rysuje na ekranie wszystkie żywe i widoczne jednostki.
     * @param units zbiór jednostek do wyrenderowania
     * @param camera kamera określająca aktualny obszar widzenia na mapie
     * @param hexSize promień heksagonu (odległość od środka do wierzchołka)
     *                wyrażony w jednostkach świata gry.
     * @param stateTime całkowity czas animacji (w sekundach) służący do wybrania odpowiedniej klatki.
     */
    fun renderUnits(
        units: Iterable<Unit>, camera: GameCamera, hexSize: Float, stateTime: Float
    ) {

        require(hexSize > 0f)

        /*
            Ustala bok kwadratowego obrazka jednostki
            na równy szerokości heksagonu (od ścianki do ścianki).
            Dzięki temu wszystkie jednostki mają jednakowy rozmiar
            wizualny względem kafelków mapy.
        */
        val unitSize = sqrt(3f) * hexSize
        val halfWidth = camera.viewportWidth * camera.zoom / 2f
        val halfHeight = camera.viewportHeight * camera.zoom / 2f

        // Wylicza prostokąt widoczny aktualnie na ekranie, uwzględniając zoom kamery.
        val cameraLeft = camera.position.x - halfWidth
        val cameraRight = camera.position.x + halfWidth

        val cameraBottom = camera.position.y - halfHeight
        val cameraTop = camera.position.y + halfHeight

        /*
            Przekazuje do SpriteBatch macierz kamery,
            dzięki której współrzędne obiektów podawane w świecie gry
            są poprawnie przekształcane do przestrzeni widoku.
        */
        batch.projectionMatrix = camera.combined
        batch.begin()

        try {
            for (unit in units) {
                // Jeśli jednostka została pokonana i nie żyje, funkcja natychmiast ją pomija.
                if (!unit.isAlive) continue

                /*
                    Przelicza współrzędne axial jednostki na pozycję X i Y
                    w świecie gry.
                    Następnie ta pozycja jest wykorzystywana do sprawdzenia,
                    czy jednostka jest przynajmniej częściowo widoczna dla kamery.
                */
                val center = axialToWorld(unit.position, hexSize, worldPosition)

                if (!isVisible(
                        center.x,
                        center.y,
                        unitSize,
                        unitSize,
                        cameraLeft,
                        cameraRight,
                        cameraBottom,
                        cameraTop
                    )
                ) {
                    continue
                }

                /*
                    Funkcja najpierw sprawdza własną podręczną mapę unitAnimations.
                    Jeśli animacja już tam jest, bierze ją od razu.
                    Jeśli nie ma jej jeszcze w pamięci podręcznej, pobiera ją z assets
                 */
                val animation = stateAnimations.getOrPut(
                    Triple(unit.civilization.id, unit.definition.id, UnitAnimation.IDLE)
                ) {
                    assets.unitAnimation(
                        unit.civilization.id, unit.definition.id, UnitAnimation.IDLE.id
                    )
                }

                /*
                    Na podstawie całkowitego czasu, jaki upłynął od uruchomienia ekranu
                    wybiera pojedynczy obrazek (TextureRegion), który ma być wyświetlony
                    w tej konkretnej ułamkowej sekundzie animacji.
                 */
                val region = animation.getKeyFrame(stateTime)

                // Rysuje wybraną klatkę animacji na środku kafelka
                batch.draw(
                    region, center.x - unitSize / 2f, center.y - unitSize / 2f, unitSize, unitSize
                )
            }
        } finally {
            batch.end()
        }
    }

    /**
     * Rysuje na ekranie widoczne miasta, dekoracje i jednostki w jednym przebiegu.
     *
     * Miasta rysowane są pod pozostałymi obiektami. Dekoracje (posortowane od tyłu do przodu)
     * są scalane w locie z jednostkami po współrzędnej Y stóp, aby zachować poprawną głębię.
     *
     * @param cities lista miast do wyrenderowania.
     * @param decorations lista dekoracji posortowana malejąco po współrzędnej Y stóp.
     * @param units lista jednostek do wyrenderowania.
     * @param camera kamera określająca aktualny obszar widzenia na mapie.
     * @param hexSize promień heksagonu (odległość od środka do wierzchołka)
     *                wyrażony w jednostkach świata gry.
     * @param stateTime całkowity czas animacji (w sekundach) służący do wybrania odpowiedniej klatki.
     * @param fadeOccluders określa, czy dekoracje zasłaniające jednostki mają być półprzezroczyste.
     * @param isTileVisible opcjonalny filtr widoczności kafelka (np. dla mgły wojny).
     */
    fun renderWorldObjects(
        cities: List<CitySprite>,
        decorations: List<DecorationSprite>,
        units: List<UnitSprite>,
        camera: GameCamera,
        hexSize: Float,
        stateTime: Float,
        fadeOccluders: Boolean,
        isTileVisible: (Hexagon) -> Boolean = { true }
    ) {
        require(hexSize > 0f)

        val pixelScale = sqrt(3f) * hexSize / TILE_TEXTURE_WIDTH_PX
        val unitSize = sqrt(3f) * hexSize
        val halfWidth = camera.viewportWidth * camera.zoom / 2f
        val halfHeight = camera.viewportHeight * camera.zoom / 2f
        val cameraLeft = camera.position.x - halfWidth
        val cameraRight = camera.position.x + halfWidth
        val cameraBottom = camera.position.y - halfHeight
        val cameraTop = camera.position.y + halfHeight

        // Sortujemy jednostki od najwyższego Y (najdalej od kamery), aby scalić je z dekoracjami.
        val sorted = units.sortedByDescending { it.y }
        var next = 0

        fun drawUnit(u: UnitSprite) {
            if (!isVisible(
                    u.x,
                    u.y,
                    unitSize,
                    unitSize,
                    cameraLeft,
                    cameraRight,
                    cameraBottom,
                    cameraTop
                )
            ) return
            val anim = if (u.moving) UnitAnimation.MOVE else UnitAnimation.IDLE
            val region = stateAnimations.getOrPut(Triple(u.civilizationId, u.unitTypeId, anim)) {
                assets.unitAnimation(u.civilizationId, u.unitTypeId, anim.id)
            }
                .getKeyFrame(stateTime)
            // Przy ruchu w lewo odbijamy sprite w poziomie (ujemna szerokość rysowania).
            if (u.flipX) batch.draw(
                region,
                u.x + unitSize / 2f,
                u.y - unitSize / 2f,
                -unitSize,
                unitSize
            )
            else batch.draw(region, u.x - unitSize / 2f, u.y - unitSize / 2f, unitSize, unitSize)
        }

        // Sprawdza, czy prostokąt dekoracji nachodzi na korpus (~60% rozmiaru) narysowanej jednostki.
        fun occludesDrawnUnit(left: Float, bottom: Float, right: Float, top: Float): Boolean {
            val half = unitSize * 0.3f
            for (i in 0 until next) {
                val u = sorted[i]
                if (u.x + half > left && u.x - half < right && u.y + half > bottom && u.y - half < top) return true
            }
            return false
        }

        batch.projectionMatrix = camera.combined
        batch.begin()
        try {
            // Miasta rysujemy w pierwszej kolejności pod wszystkimi pozostałymi obiektami.
            for (city in cities) {
                if (!isTileVisible(city.tile)) continue
                val c = axialToWorld(city.tile, hexSize, worldPosition)
                val region = cityAnimations.getOrPut(city.civilizationId to city.level) {
                    assets.cityAnimation(city.civilizationId, city.level)
                }
                    .getKeyFrame(stateTime)
                val w = unitSize
                val h = unitSize * region.regionHeight / region.regionWidth
                if (!isVisible(
                        c.x,
                        c.y,
                        w,
                        h,
                        cameraLeft,
                        cameraRight,
                        cameraBottom,
                        cameraTop
                    )
                ) continue
                batch.draw(region, c.x - w / 2f, c.y - h / 2f, w, h)
            }

            // Dekoracje i jednostki przeplatamy według pozycji stóp (algorytm malarza).
            for (sprite in decorations) {
                // Jednostki leżące "za" bieżącą dekoracją (wyższe Y stóp) rysujemy przed nią.
                while (next < sorted.size && sorted[next].y - UNIT_FOOT_OFFSET * hexSize > sprite.footY) {
                    drawUnit(sorted[next++])
                }

                if (!isTileVisible(sprite.tile)) continue

                val frames = decorationFrames.getOrPut(sprite.decorationId) {
                    assets.decorationFrames(sprite.decorationId)
                }
                val region = frames[Math.floorMod(sprite.variant, frames.size)]
                val width = region.regionWidth * pixelScale
                val height = region.regionHeight * pixelScale
                if (!isVisible(
                        sprite.footX, sprite.footY + height / 2f, width, height,
                        cameraLeft, cameraRight, cameraBottom, cameraTop
                    )
                ) continue

                // Przygaszamy dekorację, jeśli zasłania narysowaną wcześniej jednostkę.
                val faded = fadeOccluders && occludesDrawnUnit(
                    sprite.footX - width / 2f,
                    sprite.footY,
                    sprite.footX + width / 2f,
                    sprite.footY + height
                )
                if (faded) batch.setColor(1f, 1f, 1f, 0.5f)
                batch.draw(region, sprite.footX - width / 2f, sprite.footY, width, height)
                if (faded) batch.setColor(Color.WHITE)
            }
            // reszta jednostek (w modelu A: wszystkie)
            while (next < sorted.size) drawUnit(sorted[next++])
        } finally {
            batch.end()
        }
    }

    /**
     * Sprawdza, czy prostokąt reprezentujący obiekt przecina się
     * z prostokątem widocznego obszaru kamery.
     *
     * Wykorzystuje algorytm AABB,
     * polegający na sprawdzaniu przecięcia dwóch prostokątów
     * wyrównanych do osi układu współrzędnych.
     *
     * @param centerX współrzędna X środka obiektu w świecie gry.
     * @param centerY współrzędna Y środka obiektu w świecie gry.
     * @param objectWidth szerokość obiektu wyrażona w jednostkach świata gry.
     * @param objectHeight wysokość obiektu wyrażona w jednostkach świata gry.
     * @param cameraLeft lewa granica widocznego obszaru kamery.
     * @param cameraRight prawa granica widocznego obszaru kamery.
     * @param cameraBottom dolna granica widocznego obszaru kamery.
     * @param cameraTop górna granica widocznego obszaru kamery.
     * @return true, jeśli obiekt znajduje się przynajmniej częściowo
     *         w widocznym obszarze kamery lub styka się z jego granicą;
     *         false, jeśli znajduje się całkowicie poza nim.
     */
    private fun isVisible(
        centerX: Float,
        centerY: Float,
        objectWidth: Float,
        objectHeight: Float,
        cameraLeft: Float,
        cameraRight: Float,
        cameraBottom: Float,
        cameraTop: Float
    ): Boolean {

        // Wyznaczenie poziomych granic prostokąta obiektu.
        // Od środka obiektu odejmujemy lub dodajemy połowę jego szerokości.
        val objectLeft = centerX - objectWidth / 2f
        val objectRight = centerX + objectWidth / 2f

        // Wyznaczenie pionowych granic prostokąta obiektu.
        // Od środka obiektu odejmujemy lub dodajemy połowę jego wysokości.
        val objectBottom = centerY - objectHeight / 2f
        val objectTop = centerY + objectHeight / 2f/*
            Sprawdzamy, czy prostokąt obiektu przecina się
            z prostokątem widocznego obszaru kamery.
         */
        return objectRight >= cameraLeft && objectLeft <= cameraRight && objectTop >= cameraBottom && objectBottom <= cameraTop
    }

    /**
     * Konwertuje współrzędne heksagonu z układu axial na pozycję w świecie gry
     * dla orientacji Pointy-Top.
     *
     * @param coordinate współrzędne heksagonu w układzie axial (q, r).
     *                   Współrzędna s układu Cube jest wyliczana na podstawie q i r.
     * @param hexSize promień heksagonu (odległość od środka do wierzchołka)
     *                wyrażony w jednostkach świata gry.
     * @param out istniejący wektor Vector2, do którego zostanie zapisana
     *            obliczona pozycja. Pozwala uniknąć tworzenia nowego obiektu.
     * @return ten sam wektor out zawierający obliczoną pozycję w świecie gry.
     */
    fun axialToWorld(
        coordinate: Hexagon, hexSize: Float, out: Vector2
    ): Vector2 {
        return HexLayout.axialToWorld(coordinate, hexSize, out)
    }

    /**
     * Konwertuje pozycję w świecie gry na współrzędne heksagonu w układzie axial
     * dla orientacji Pointy-Top.
     *
     * Wybiera najbliższy heks na podstawie podanej pozycji w świecie gry.
     *
     * @param worldX współrzędna X pozycji w świecie gry,
     *               wyrażona w jednostkach świata.
     * @param worldY współrzędna Y pozycji w świecie gry,
     *               wyrażona w jednostkach świata.
     * @param hexSize promień heksagonu (odległość od środka do wierzchołka),
     *                wyrażony w jednostkach świata gry.
     * @return współrzędne najbliższego heksagonu w układzie axial (q, r).
     *         Współrzędna s układu Cube jest wyliczana na podstawie q i r.
     */
    fun worldToAxial(
        worldX: Float, worldY: Float, hexSize: Float
    ): Hexagon {
        return HexLayout.worldToAxial(worldX, worldY, hexSize)
    }

    fun dispose() {
        batch.dispose()
    }

    /**
     * Czyści cache animacji po odbudowie atlasów przez GameAssets.
     *
     * Stare animacje mogą zawierać referencje do TextureRegion
     * pochodzących ze zwolnionych tekstur.
     * Dzięki wyczyszczeniu cache kolejne renderowanie pobierze
     * animacje z aktualnych atlasów.
     */
    fun onAssetsRebuilt() {
        stateAnimations.clear()
        cityAnimations.clear()
        decorationFrames.clear()
    }
}
