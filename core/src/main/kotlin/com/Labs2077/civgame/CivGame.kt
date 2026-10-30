package com.Labs2077.civgame

import com.Labs2077.civgame.client.rules.RulesetRepository
import com.Labs2077.civgame.engine.rules.units.UnitRuleset
import com.Labs2077.civgame.engine.rules.units.UnitRulesetFile
import com.Labs2077.civgame.engine.rules.terrain.TerrainRuleset
import com.Labs2077.civgame.engine.rules.terrain.TerrainRulesetFile
import com.badlogic.gdx.Game

class CivGame : Game() {
    lateinit var unitRuleset: UnitRuleset
        private set
    /**
     * Zestaw definicji wszystkich rodzajów terenu dostępnych w grze.
     *
     * Przechowuje informacje o właściwościach terenów, takich jak ich
     * przechodniość, koszt ruchu oraz zasady produkcji zasobów.
     * Umożliwia wyszukiwanie definicji terenu na podstawie jego ID.
     *
     * Definicje są wczytywane z pliku terrains.json i sprawdzane
     * pod kątem poprawności przed udostępnieniem ich grze.
     */
    lateinit var terrainRuleset: TerrainRuleset
        private set
    /**
     * Odpowiada za wczytywanie plików konfiguracyjnych z assets
     * i przekształcanie ich w obiekty wykorzystywane przez grę
     */
    val repository = RulesetRepository()

    override fun create() {
        // Póki co wczytujemy reguły jednostek po uruchomieniu samej gry, ale docelowo będzie to wczytywane przy
        // rozpoczęciu gry w menu lub wczytaniu gry.
        unitRuleset = repository.load(
            path = "rules/units.json",
            deserializer = UnitRulesetFile.serializer(),
            createRuleset = UnitRuleset.Companion::fromFile
        )
        terrainRuleset = repository.load(
            path = "rules/terrains.json",
            deserializer = TerrainRulesetFile.serializer(),
            createRuleset = TerrainRuleset.Companion::fromFile
        )
    }

    override fun resume() {
        super.resume()
    }

    override fun dispose() {
        super.dispose()
    }
}
