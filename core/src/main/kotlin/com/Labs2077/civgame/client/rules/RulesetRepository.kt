package com.Labs2077.civgame.client.rules

import com.Labs2077.civgame.engine.rules.RulesetJson
import com.badlogic.gdx.Gdx
import kotlinx.serialization.DeserializationStrategy

class RulesetRepository {
    fun <F, R> load(
        path: String,
        deserializer: DeserializationStrategy<F>,
        createRuleset: (F) -> R
    ): R {
        val source = Gdx.files.internal(path).readString("UTF-8")
        val file = RulesetJson.decode(source, deserializer)

        return createRuleset(file)
    }
}
