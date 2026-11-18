package com.Labs2077.civgame.data.repository

import kotlinx.serialization.json.Json

val RulesetJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = false
    isLenient = false
}
