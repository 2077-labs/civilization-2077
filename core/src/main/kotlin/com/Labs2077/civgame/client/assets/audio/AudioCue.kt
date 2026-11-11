package com.Labs2077.civgame.client.assets.audio

/**
 * Rodzaje dźwięków w grze, będą wykorzystywane, aby skojarzyć wydarzenia z rzeczywistymi plikami audio w ramach klasy GameAudio.
 */
enum class AudioCue {
    UI_CLICK,
    TURN_END,

    BUILD_COMPLETED,
    CITY_FOUNDED,

    RIFLE_FIRE,
    LASER_FIRE,
    ROCKET_FIRE,
    TANK_FIRE,

    UNIT_HIT,
    EXPLOSION,

    MECH_STEP,
    RADAR_PING,

    ERROR;

    companion object {
        val ALL = entries.toSet()
    }
}
