package assets

import com.Labs2077.civgame.client.units.UnitAnimation
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import java.io.File
import javax.imageio.ImageIO

/**
 * Pakuje wszystkie grafiki PNG do dwóch atlasów i kopiuje dźwięki do assets/. Do uruchomienia wyłącznie przy wykorzystaniu
 * lwjgl3 (ponieważ w tym module dodałem zależność do libgdx-tools, która nie jest dostępna w module core).
 * Układ źródeł (raw/):
 *   raw/terrain, raw/icons, raw/fx, raw/common/missing.png   ->  assets/atlas/common.atlas  (bez koloru graczy)
 *   raw/units, raw/cities                                    ->  assets/atlas/units.atlas   (magenta -> kolor gracza)
 *   raw/sfx, raw/music                                       ->  kopiowane do assets/sfx, assets/music (bez pakowania)
 *
 * Nazwa regionu = nazwa pliku bez rozszerzenia i bez końcówki _<numer>, np. tank_idle_3.png -> region "tank_idle".
 * Co istotne nie było możliwe dynamiczne generowanie grafik w locie przed uruchomieniem gry, ponieważ na Androidzie TexturePacker
 * nie jest dostępny. Wynika to z tego, że gdx-tools korzysta chociażby z java.awt i ImageIO, które nie są dostępne w Androidzie.
 */
private const val RAW = "raw"
private const val STAGING = "build/atlas-staging"
private const val OUT = "assets/atlas"
private const val MAX_WIDTH = 2048
private const val MAX_HEIGHT = 2048
private const val PADDING_X = 2
private const val PADDING_Y = 2

private class AtlasSpec(val name: String, val folders: List<String>, val checkUnits: Boolean = false)

private val ATLASES = listOf(
    AtlasSpec("common", listOf("terrain", "icons", "fx", "common")),
    // miasta mają sztandar w magencie, więc razem z jednostkami przechodzą wypiekanie kolorów graczy
    AtlasSpec("units", listOf("units", "cities"), checkUnits = true)
)
private val UNIT_ANIM = Regex("^(.+)_(${UnitAnimation.entries.joinToString("|") { it.id }})_\\d+\\.png$")
private val FRAME = Regex("^(.+)_\\d+\\.png$")
// Dla uproszczenia walidacji zakładam, że wszystkie jednostki muszą mieć dokładnie wszystkie rodzaje animacji, aby nie
// komplikować, a komunikaty można zignorować dla jednostek cywilnych
private val UNIT_REQUIRED_ANIMATIONS = UnitAnimation.ALL.toList()

fun main() {
    val settings = TexturePacker.Settings().apply {
        maxWidth = MAX_WIDTH
        maxHeight = MAX_HEIGHT
        paddingX = PADDING_X
        paddingY = PADDING_Y
        duplicatePadding = true
        filterMin = Texture.TextureFilter.Nearest
        filterMag = Texture.TextureFilter.Nearest
        stripWhitespaceX = false
        stripWhitespaceY = false
    }
    File(OUT).mkdirs()
    for (spec in ATLASES) {
        val dir = stage(spec)
        validate(dir, spec.checkUnits)
        TexturePacker.process(settings, dir.path, OUT, spec.name)
        println("Atlas '${spec.name}' gotowy: $OUT/${spec.name}.atlas (${dir.listFiles()!!.size} plików PNG)")
    }
    copyAudio()
}

/**
 * Zbiera PNG-i z kilku folderów do jednego płaskiego folderu tymczasowego. Dzięki temu nazwy regionów
 * nie zawierają nazw podfolderów (np. "units/tank_idle")
 */
private fun stage(spec: AtlasSpec): File {
    val dir = File(STAGING, spec.name)
    dir.deleteRecursively()
    dir.mkdirs()
    val seen = HashMap<String, String>()
    for (folder in spec.folders) {
        val src = File(RAW, folder)
        require(src.isDirectory) { "Brak folderu ${src.path}" }
        for (f in src.walkTopDown().filter { it.isFile && it.extension.equals("png", ignoreCase = true) }) {
            val previous = seen.put(f.name, f.path)
            require(previous == null) { "Duplikat nazwy pliku ${f.name}: $previous oraz ${f.path}" }
            f.copyTo(File(dir, f.name))
        }
    }
    return dir
}

/** Tylko ostrzeżenia, niczego nie blokuje. Łapie typowe błędy w nazwach i rozmiarach klatek. */
private fun validate(dir: File, units: Boolean) {
    val pngs = dir.listFiles { f -> f.extension.equals("png", ignoreCase = true) }!!.sortedBy { it.name }
    val sizes = pngs.associateWith { f -> ImageIO.read(f).let { it.width to it.height } }

    // wszystkie klatki jednej animacji muszą mieć ten sam rozmiar
    pngs.groupBy { FRAME.matchEntire(it.name)?.groupValues?.get(1) ?: it.nameWithoutExtension }
        .forEach { (region, files) ->
            val s = files.map { sizes.getValue(it) }.toSet()
            if (s.size > 1) println("UWAGA [$region]: klatki mają różne rozmiary: $s")
        }
    if (!units) return

    // jednostka: obowiązkowe animacje i ten sam rozmiar płótna we wszystkich animacjach
    val byUnit = pngs.mapNotNull { f -> UNIT_ANIM.matchEntire(f.name)?.let { Triple(it.groupValues[1], it.groupValues[2], f) } }
        .groupBy { it.first }
    for ((unit, items) in byUnit) {
        val anims = items.map { it.second }.toSet()
        for (unitRequiredAnim in UNIT_REQUIRED_ANIMATIONS) {
            if (unitRequiredAnim.toString().lowercase() !in anims) println("UWAGA [$unit]: brak animacji $unitRequiredAnim (sa: $anims)")
        }
        val s = items.map { sizes.getValue(it.third) }.toSet()
        if (s.size > 1) println("UWAGA [$unit]: animacje mają różne rozmiary płótna: $s")
    }
}

private fun copyAudio() {
    for (dir in listOf("sfx", "music")) {
        val src = File(RAW, dir)
        if (src.isDirectory) {
            src.copyRecursively(File("assets", dir), overwrite = true)
            println("Skopiowano $dir -> assets/$dir")
        }
    }
}
