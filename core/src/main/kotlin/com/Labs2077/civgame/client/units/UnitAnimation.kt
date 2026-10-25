package com.Labs2077.civgame.client.assets

import com.Labs2077.civgame.engine.units.UnitAbility
import com.Labs2077.civgame.engine.units.UnitDefinition

/** Id animacji odpowiada końcówce nazwy regionu w atlasie, np. scout_idle_1.png. */
enum class UnitAnimation {
    IDLE, ATTACK, MOVE;

    val id: String = name.lowercase()
}

/**
 * Wymagane animacje wyliczamy ze statystyk/umiejętności, a nie z UnitKind.
 * Dzięki temu osadnik i robotnik nie wymagają animacji ataku.
 */
fun UnitDefinition.requiredAnimations(): Set<UnitAnimation> = buildSet {
    add(UnitAnimation.IDLE)
    if (movement > 0) add(UnitAnimation.MOVE)
    if (UnitAbility.ATTACK_MELEE in abilities || UnitAbility.ATTACK_RANGED in abilities) {
        add(UnitAnimation.ATTACK)
    }
}
