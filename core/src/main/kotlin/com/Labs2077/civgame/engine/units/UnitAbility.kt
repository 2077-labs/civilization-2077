package com.Labs2077.civgame.engine.units

import kotlinx.serialization.Serializable

/**
 * Możliwości jednostki. Silnik używa ich do walidacji akcji, a klient może na ich podstawie
 * pokazywać odpowiednie przyciski.
 */
@Serializable
enum class UnitAbility {
    /** Może założyć miasto, jeśli spełnione są warunki na mapie. */
    FOUND_CITY,

    /** Może wykonać atak wręcz. */
    ATTACK_MELEE,

    /** Może wykonać atak dystansowy; zasięg określa attackRange. */
    ATTACK_RANGED,

    /** Nie może zaatakować w turze, w której wcześniej się poruszyła (np. artyleria rakietowa). */
    CANNOT_ATTACK_AFTER_MOVING
}
