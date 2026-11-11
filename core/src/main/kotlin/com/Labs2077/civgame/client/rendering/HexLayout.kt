package com.Labs2077.civgame.client.rendering

import com.Labs2077.civgame.engine.map.HexCoordinate
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

object HexLayout {

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
        // Promień hexa nie może być mniejszy lub równy 0
        require(hexSize > 0f)

        val q = coordinate.q.toFloat()
        val r = coordinate.r.toFloat()

        /*
         * Układ Pointy-Top:
         *
         * X:
         * sqrt(3f) * hexSize określa szerokość heksagonu
         * (od lewej płaskiej krawędzi do prawej).
         *
         * q odpowiada za przesunięcie poziome o pełną szerokość
         * heksagonu dla każdego kolejnego heksu.
         *
         * r / 2f dodaje poziome przesunięcie o połowę szerokości
         * heksagonu dla każdego kolejnego rzędu.
         * Dzięki temu sąsiednie rzędy są przesunięte względem siebie,
         * a heksagony zazębiają się bez pozostawiania przerw.
         *
         * Y:
         * 1.5f * hexSize określa odległość pionową między środkami
         * sąsiednich rzędów heksagonów.
         *
         * Całkowita wysokość heksagonu wynosi 2 * hexSize,
         * natomiast odległość między środkami rzędów wynosi 1.5 * hexSize.
         * Oznacza to, że sąsiednie rzędy nachodzą na siebie o 0.5 * hexSize.
         *
         * Mnożąc tę odległość przez r, otrzymujemy pionowe położenie
         * środka heksagonu w świecie gry.
        */
        return out.set(
            // X
            sqrt(3f) * hexSize * (q + r / 2f),
            // Y
            1.5f * hexSize * r
        )
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
        // Promień hexa nie może być mniejszy lub równy 0
        require(hexSize > 0f)

        /*
        * Wzory uzyskujemy po wyznaczeniu funkcji odwrotnych do
        * odpowiedników w konwersji axialToWorld
         */
        val qFloat = (sqrt(3f) / 3f * worldX - worldY / 3f) / hexSize
        val rFloat = (2f / 3f * worldY) / hexSize
        val sFloat = -qFloat - rFloat

        // Zaokrąglamy wszystkie trzy współrzędne do najbliższych liczb całkowitych.
        var q = qFloat.roundToInt()
        var r = rFloat.roundToInt()
        val s = sFloat.roundToInt()

        /*
         * Obliczamy błąd zaokrąglenia każdej współrzędnej.
         *
         * Po niezależnym zaokrągleniu q, r i s ich suma może być różna od 0,
         * mimo że w układzie Cube musi być spełniony warunek:
         * q + r + s = 0.
         */
        val qDifference = abs(q - qFloat)
        val rDifference = abs(r - rFloat)
        val sDifference = abs(s - sFloat)

        /*
        * Korygujemy współrzędną, która ma największy błąd zaokrąglenia.
        * Wyznaczamy ją na podstawie dwóch pozostałych współrzędnych,
        * aby zachować warunek q + r + s = 0.
        *
        * Jeżeli największy błąd dotyczy s, nie musimy jawnie go poprawiać,
        * ponieważ HexCoordinate przechowuje q i r, a s jest wyliczane
        * jako -q - r.
        */
        if (qDifference > rDifference && qDifference > sDifference) {
            q = -r - s
        } else if (rDifference > sDifference) {
            r = -q - s
        }
        // Współrzędne systemu axial
        return HexCoordinate(q, r)
    }
}
