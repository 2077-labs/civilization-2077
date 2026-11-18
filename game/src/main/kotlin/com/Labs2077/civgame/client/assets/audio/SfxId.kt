package com.Labs2077.civgame.client.assets.audio

/**
 * Identyfikatory dźwięków w grze, aby uniknąć pomyłek przy ich wywoływaniu.
 *
 * Nazwa pliku to "sfx/<id>.wav".
 * Wszystkie zasoby są ładowane jako LibGDX Sound.
 */
enum class SfxId(val fileName: String) {
    BUILD_DONE("build_done"),
    CITY_FOUNDED("city_founded"),

    ENGINE_LOOP("engine_loop"),

    ERROR_BUZZ("error_buzz"),
    EXPLOSION("explosion"),

    GEIGER_LOOP("geiger_loop"),

    LASER_SHOT("laser_shot"),
    LASER_SHOT_B("laser_shot_b"),

    MECH_STEP("mech_step"),
    MECH_STEP_B("mech_step_b"),

    RADAR_PING("radar_ping"),

    RAIN_LOOP("rain_loop"),

    RIFLE_SHOT("rifle_shot"),
    RIFLE_SHOT_B("rifle_shot_b"),

    ROCKET_LAUNCH("rocket_launch"),

    SANDSTORM_LOOP("sandstorm_loop"),

    TANK_CANNON("tank_cannon"),

    TURN_END("turn_end"),
    UI_CLICK("ui_click"),

    UNIT_HIT("unit_hit"),
    UNIT_HIT_B("unit_hit_b");

    val path: String
        get() = "sfx/$fileName.wav"

    companion object {
        val ALL = entries.toSet()
    }
}
