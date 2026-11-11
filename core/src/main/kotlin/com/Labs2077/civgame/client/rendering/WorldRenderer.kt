package com.Labs2077.civgame.client.rendering

import com.Labs2077.civgame.client.assets.AssetProvider
import com.Labs2077.civgame.client.rendering.camera.GameCamera
import com.Labs2077.civgame.client.units.UnitAnimation
import com.Labs2077.civgame.engine.map.HexCoordinate
import com.Labs2077.civgame.engine.map.MapTile
import com.Labs2077.civgame.engine.rules.UnitId
import com.Labs2077.civgame.engine.units.UnitInstance
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import kotlin.math.sqrt

class WorldRenderer(
    private val assets: AssetProvider
) {
    private val batch = SpriteBatch()
    private val worldPosition = Vector2()

    /*
     * Cache animacji jednostek.
     *
     * Klucz składa się z ID właściciela i ID typu jednostki,
     * dzięki czemu każdy gracz może korzystać z animacji
     * przygotowanej dla swojej frakcji.
     *
     * Animacje są pobierane z GameAssets tylko wtedy,
     * gdy nie ma ich jeszcze w cache.
     */
    private val unitAnimations =
        mutableMapOf<Pair<Int, UnitId>, Animation<TextureRegion>>()

    /**
     * Rysuje na ekranie wszystkie widoczne kafelki
     * @param tiles zbiór kafelków
     * @param camera kamera, która wyświetli widoczne kafelki
     * @param hexSize promień heksagonu (odległość od środka do wierzchołka)
     *                wyrażony w jednostkach świata gry.
     */
    fun renderTiles(
        tiles: Iterable<MapTile>,
        camera: GameCamera,
        hexSize: Float
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
                val region = assets.terrain(tile.terrainId)

                // Rysowanie grafiki na ekranie
                batch.draw(
                    region,
                    // Wyznaczamy lewy dolny róg grafiki,
                    // odejmując połowę szerokości i wysokości od środka kafelka.
                    center.x - tileWidth / 2f,
                    center.y - tileHeight / 2f,
                    tileWidth,
                    tileHeight
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
        units: Iterable<UnitInstance>,
        camera: GameCamera,
        hexSize: Float,
        stateTime: Float
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

                // Tworzymy klucz cache na podstawie właściciela i typu jednostki.
                val key = unit.ownerId to unit.definitionId


                /*
                    Funkcja najpierw sprawdza własną podręczną mapę unitAnimations.
                    Jeśli animacja już tam jest, bierze ją od razu.
                    Jeśli nie ma jej jeszcze w pamięci podręcznej, pobiera ją z assets
                 */
                val animation = unitAnimations.getOrPut(key) {
                    assets.unitAnimation(
                        unit.ownerId,
                        unit.definitionId,
                        UnitAnimation.IDLE.id
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
                    region,
                    center.x - unitSize / 2f,
                    center.y - unitSize / 2f,
                    unitSize,
                    unitSize
                )
            }
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
        val objectTop = centerY + objectHeight / 2f
        /*
            Sprawdzamy, czy prostokąt obiektu przecina się
            z prostokątem widocznego obszaru kamery.
         */
        return objectRight >= cameraLeft &&
                objectLeft <= cameraRight &&
                objectTop >= cameraBottom &&
                objectBottom <= cameraTop
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
        coordinate: HexCoordinate,
        hexSize: Float,
        out: Vector2
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
        worldX: Float,
        worldY: Float,
        hexSize: Float
    ): HexCoordinate {
        return HexLayout.worldToAxial(worldX, worldY, hexSize)
    }

    fun dispose() {
        batch.dispose()
    }

    /*
    * Czyści cache animacji po odbudowie atlasów przez GameAssets.
    *
    * Stare animacje mogą zawierać referencje do TextureRegion
    * pochodzących ze zwolnionych tekstur.
    * Dzięki wyczyszczeniu cache kolejne renderowanie pobierze
    * animacje z aktualnych atlasów.
    */
    fun onAssetsRebuilt() {
        unitAnimations.clear()
    }
}
