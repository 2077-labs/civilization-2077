package com.Labs2077.civgame.data.repository

import com.badlogic.gdx.Gdx
import kotlinx.serialization.json.Json

abstract class AbstractGdxRepository {

    protected abstract val configuration: Json

    protected inline fun <reified T> decode(path: String): T {
        val source = Gdx.files.internal(path)
            .readString(Charsets.UTF_8.name())
        return configuration.decodeFromString(source)
    }
}
