package com.Labs2077.civgame

import com.Labs2077.civgame.client.rules.RulesetRepository
import com.Labs2077.civgame.engine.rules.units.UnitRuleset
import com.Labs2077.civgame.engine.rules.units.UnitRulesetFile
import com.badlogic.gdx.Game

class CivGame : Game() {
    lateinit var unitRuleset: UnitRuleset
        private set
    val repository = RulesetRepository()

    override fun create() {
        // Póki co wczytujemy reguły jednostek po uruchomieniu samej gry, ale docelowo będzie to wczytywane przy
        // rozpoczęciu gry w menu lub wczytaniu gry.
        unitRuleset = repository.load(
            path = "rules/units.json",
            deserializer = UnitRulesetFile.serializer(),
            createRuleset = UnitRuleset.Companion::fromFile
        )
    }

    override fun resume() {
        super.resume()
    }

    override fun dispose() {
        super.dispose()
    }
}
