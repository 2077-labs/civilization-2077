package com.Labs2077.civgame.client.decoration

import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.Terrain
import kotlin.random.Random

/**
 * Zawiera informacje o tym, jakie dekoracje i gdize stoją na kafelkach:
 * variant to surowa liczba, jest ona redukowana modulo liczbą wariantów w atlasie.
 *
 * @property offsetX przesunięcie stóp względem środka heksa, -1..1 (znormalizowane)
 * @property offsetY jak wyżej (pion)
 */
data class PlacedDecoration(
    val decorationId: DecorationId,
    val variant: Int,
    val offsetX: Float,
    val offsetY: Float
)

object DecorationPlanner {
    /**
     * Generuje plan dekoracji dla całej mapy. Kafelki z [suppressed] (np. miasta)
     * nie dostają dekoracji.
     */
    fun planMap(
        tiles: Iterable<Terrain>,
        ruleset: DecorationRuleset,
        mapSeed: Long,
        suppressed: Set<Hexagon> = emptySet()
    ): Map<Hexagon, List<PlacedDecoration>> {
        val result = HashMap<Hexagon, List<PlacedDecoration>>()
        for (tile in tiles) {
            if (tile.position in suppressed) continue
            val config = ruleset.definitions[tile.definition.id] ?: continue
            val placed = plan(tile.position, config, mapSeed)
            if (placed.isNotEmpty()) result[tile.position] = placed
        }
        return result
    }

    /**
     * Metoda generująca plan dekoracji dla pojedynczego kafelka. Bierze pod uwagę
     * kombinację pozycji, seeda i definicji terenu, aby zapewnić powtarzalność wyników.
     */
    fun plan(position: Hexagon, config: TerrainDecorations, mapSeed: Long): List<PlacedDecoration> {
        val rng = Random(tileSeed(mapSeed, position))

        // Jeśli wylosujemy, że nie ma dekoracji, to zwracamy pustą listę.
        if (rng.nextFloat() >= config.chance) return emptyList()
        // Określa ilość dekoracji, które mają się pojawić
        val count = rng.nextInt(config.minCount, config.maxCount + 1)

        val used = HashMap<DecorationId, Int>()
        val result = ArrayList<PlacedDecoration>(count)
        repeat(count) {
            // Możliwe do wyboru są jeszcze tylko te dekoracje, które nie przekroczyły
            // limitu wystąpień.
            val available = config.rules.filter {
                used.getOrDefault(it.decorationId, 0) < it.maxPerTile
            }
            if (available.isEmpty()) return result
            // Wybieramy losowo jedną z reguł dekoracji z uwzględnieniem podanych wag występowania.
            val rule = pickWeighted(available, rng)
            used.merge(rule.decorationId, 1, Int::plus)
            result += PlacedDecoration(
                decorationId = rule.decorationId,
                // Wariant jest losowy, ale powtarzalny dla danego kafelka i seeda mapy.
                variant = rng.nextInt(Int.MAX_VALUE),
                // Podobnie jest z przesunięciami, które są generowane w zakresie -1..1.
                offsetX = rng.nextFloat() * 2f - 1f,
                offsetY = rng.nextFloat() * 2f - 1f
            )
        }
        return result
    }

    /**
     * Wybiera losowo regułę dekoracji z listy reguł z uwzględnieniem wag.
     * Realizuje to tzw. Weighted Random Selection, czyli losowanie ważone.
     * Prawdopodobieństwo wyboru reguły wynosi jej waga podzielona przez sumę wag
     * wszystkich reguł.
     */
    private fun pickWeighted(rules: List<DecorationRule>, rng: Random): DecorationRule {
        // Z uwagi na powyższe założenia, generujemy losową liczbę całkowitą z zakresu od 0 do
        // sumy wag wszystkich reguł.
        var roll = rng.nextInt(rules.sumOf { it.weight })
        // Zwracamy pierwszą regułę, dla której po odjęciu jej wagi od dotychczasowej sumy
        // dostajemy liczbę ujemną.
        for (rule in rules) {
            roll -= rule.weight
            if (roll < 0) return rule
        }
        return rules.last()
    }

    /**
     * Generowanie ziarna dla danego kafelka na podstawie ziarna mapy i współrzędnych kafelka.
     */
    private fun tileSeed(mapSeed: Long, p: Hexagon): Long {
        // Najpierw łączymy współrzędne (które są zapisane na 4 bajtach) w jedną liczbę
        // 8 bajtową, a następnie mieszamy ją z ziarnem mapy (która to już jest liczbą
        // 8 bajtową).
        val packed = (p.q.toLong() shl 32) xor (p.r.toLong() and 0xFFFFFFFFL)
        // Samo mapSeed xor packed nie jest wystarczająco dobre, bo dla małych map (np. 10x10)
        // daje bardzo podobne wyniki. Wówczas lepiej dodać jeszcze mieszanie, które dodatkowo
        // rozproszy bity.
        return mix(mapSeed xor mix(packed))
    }

    /**
     * Funkcja mieszająca realizująca algorytm "splitmix64" (https://prng.di.unimi.it/splitmix64.c).
     */
    private fun mix(value: Long): Long {
        var z = value + 0x9E3779B97F4A7C15uL.toLong()
        z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9uL.toLong()
        z = (z xor (z ushr 27)) * 0x94D049BB133111EBuL.toLong()
        return z xor (z ushr 31)
    }
}
