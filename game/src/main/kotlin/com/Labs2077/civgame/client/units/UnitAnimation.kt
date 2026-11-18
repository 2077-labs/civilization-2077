package com.Labs2077.civgame.client.units

import com.labs2077.civilization.core.models.UnitAbility
import com.labs2077.civilization.core.models.UnitDefinition

/** Id animacji odpowiada końcówce nazwy regionu w atlasie, np. scout_idle_1.png. */
enum class UnitAnimation {
    IDLE, ATTACK, MOVE;

    val id: String = name.lowercase()

    companion object {
        val ALL: Set<UnitAnimation> = entries.toSet()
    }
}

/**
 * Wymagane animacje wyliczamy ze statystyk/umiejętności.
 * Dzięki temu osadnik i robotnik nie wymagają animacji ataku.
 */
fun UnitDefinition.requiredAnimations(): Set<UnitAnimation> = buildSet {
    add(UnitAnimation.IDLE)
    if (movement > 0) {
        add(UnitAnimation.MOVE)
    }
    if (UnitAbility.ATTACK in abilities) {
        add(UnitAnimation.ATTACK)
    }
}
