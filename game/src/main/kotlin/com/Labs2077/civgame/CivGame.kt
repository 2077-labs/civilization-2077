package com.Labs2077.civgame

import com.Labs2077.civgame.client.assets.GameAssets
import com.Labs2077.civgame.client.assets.audio.GameAudio
import com.Labs2077.civgame.client.screens.MenuLoadingScreen
import com.Labs2077.civgame.data.repository.rulesets.BuildingDefinitionGdxFilesRepository
import com.Labs2077.civgame.data.repository.rulesets.CityLevelDefinitionGdxFilesRepository
import com.Labs2077.civgame.data.repository.rulesets.TerrainDefinitionGdxFilesRepository
import com.Labs2077.civgame.data.repository.rulesets.UnitDefinitionGdxFilesRepository
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.labs2077.civilization.core.models.BuildingRuleset
import com.labs2077.civilization.core.models.CityLevelRuleset
import com.labs2077.civilization.core.models.TerrainRuleset
import com.labs2077.civilization.core.models.UnitRuleset

class CivGame : Game() {
    lateinit var assets: GameAssets
        private set
    lateinit var gameAudio: GameAudio
        private set

    lateinit var buildingRuleset: BuildingRuleset private set
    lateinit var cityLevelRuleset: CityLevelRuleset private set
    lateinit var unitRuleset: UnitRuleset private set

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
    lateinit var terrainRuleset: TerrainRuleset private set

    /**
     * Odpowiada za wczytywanie plików konfiguracyjnych z assets
     * i przekształcanie ich w obiekty wykorzystywane przez grę
     */
    val buildingRepository = BuildingDefinitionGdxFilesRepository("rules/buildings.json")
    val cityLevelRepository = CityLevelDefinitionGdxFilesRepository("rules/city_levels.json")
    val terrainRepository = TerrainDefinitionGdxFilesRepository("rules/terrains.json")
    val unitRepository = UnitDefinitionGdxFilesRepository("rules/units.json")

    override fun create() {
        // Póki co wczytujemy reguły jednostek po uruchomieniu samej gry, ale docelowo będzie to wczytywane przy
        // rozpoczęciu gry w menu lub wczytaniu gry.
        buildingRuleset = BuildingRuleset(buildingRepository.getAll())
        cityLevelRuleset = CityLevelRuleset(cityLevelRepository.getAll())
        unitRuleset = UnitRuleset(unitRepository.getAll())
        terrainRuleset = TerrainRuleset(terrainRepository.getAll())


        assets = GameAssets()
        gameAudio = GameAudio(assets::sound)
        assets.queueBase()
        /** Test dźwięków i pętli audio. */
//        assets.finishLoading()
//        gameAudio.syncLoops(
//            desired = mapOf(
//                LoopKey(LoopSource.Ambient("ambient"), LoopSlot.AMBIENCE) to LoopRequest(LoopCue.ENGINE, volume = 0.5f),
//                LoopKey(LoopSource.Ambient("other_ambient"), LoopSlot.AMBIENCE) to LoopRequest(LoopCue.GEIGER, volume = 0.5f)
//            )
//        )
//
//        println("other_ambient is playing: ${gameAudio.isLoopPlaying(LoopKey(LoopSource.Ambient("other_ambient"), LoopSlot.AMBIENCE))}")
//
//
//        Thread.sleep(4000)
//        // Should disable the other_ambient loop, because it's not in the desired map anymore
//        gameAudio.syncLoops(
//            desired = mapOf(
//                LoopKey(LoopSource.Ambient("ambient"), LoopSlot.AMBIENCE) to LoopRequest(LoopCue.ENGINE, volume = 0.5f)
//            )
//        )
//        println("other_ambient is playing: ${gameAudio.isLoopPlaying(LoopKey(LoopSource.Ambient("other_ambient"), LoopSlot.AMBIENCE))}")
//        Thread.sleep(4000)
//        gameAudio.play(AudioCue.ROCKET_FIRE, volume = 0.8f)

        setScreen(MenuLoadingScreen(this))
    }

    override fun render() {
        gameAudio.update(Gdx.graphics.deltaTime)

        super.render()
    }

    override fun resume() {
        super.resume()
        // Odbudowa wypieczonych atlasów po utracie kontekstu OpenGL
        assets.onResume()
    }

    override fun dispose() {
        super.dispose()
        gameAudio.dispose()
        // Zwalnianie zasobów gry (atlasów, dźwięków) przy zamykaniu gry
        assets.dispose()
    }
}
