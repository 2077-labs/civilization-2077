package com.Labs2077.civgame.client.screens

enum class WorldSizeOption(
    val displayName: String,
    val width: Int,
    val height: Int,
) {
    SMALL("Mała", 10, 10),
    MEDIUM("Średnia", 20, 20),
    LARGE("Duża", 30, 30),
}

data class NewGameSettings(
    val worldSize: WorldSizeOption = WorldSizeOption.MEDIUM,
    val seed: Long? = null,
    val playerCount: Int = 2,
) {
    init {
        require(playerCount in 2..8) {
            "Liczba graczy musi wynosić od 2 do 8."
        }
    }
}
