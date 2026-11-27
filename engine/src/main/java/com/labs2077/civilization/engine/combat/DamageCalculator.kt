package com.labs2077.civilization.engine.combat

import com.labs2077.civilization.core.models.UnitDefinition
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Oblicza obrażenia zadawane w walce według wzoru z koncepcji:
 *
 * `obrażenia = A × 2 × A / (A + D) × bonus × losowość(0,9–1,1)`
 *
 * gdzie A to atak atakującego, a D to obrona broniącego się.
 *
 * Wynik jest zaokrąglany do liczby całkowitej i wynosi co najmniej 1,
 * żeby każdy atak jednostki posiadającej siłę ataku coś zadawał.
 *
 * @param randomFactor źródło losowości z przedziału 0,9–1,1.
 */
internal class DamageCalculator(
    private val randomFactor: () -> Double = {
        Random.nextDouble(
            MIN_RANDOM_FACTOR,
            MAX_RANDOM_FACTOR
        )
    },
) {

    /** Obrażenia zadane przez [attacker] jednostce [defender] w zwykłym ataku. */
    fun attackDamage(attacker: UnitDefinition, defender: UnitDefinition): Int {
        return calculate(attacker, defender, multiplier = 1.0)
    }

    /** Obrażenia zadane przez [defender] (kontratakujący) jednostce [attacker] (pierwotny atakujący). Moc kontrataku wynosi 50%. */
    fun counterAttackDamage(defender: UnitDefinition, attacker: UnitDefinition): Int {
        return calculate(defender, attacker, multiplier = COUNTER_ATTACK_MULTIPLIER)
    }

    private fun calculate(
        attacker: UnitDefinition,
        defender: UnitDefinition,
        multiplier: Double
    ): Int {
        val attack = attacker.attack.toDouble()
        val defense = defender.defense.toDouble()

        if (attack <= 0.0) {
            return 0
        }

        val base = attack * 2.0 * attack / (attack + defense)
        val damage = base * bonus(attacker, defender) * multiplier * randomFactor()

        return damage.roundToInt()
            .coerceAtLeast(MINIMUM_DAMAGE)
    }

    /** Jednostki dystansowe zadają podwójne obrażenia jednostkom pancernym (pojazd/czołg/mech). */
    private fun bonus(attacker: UnitDefinition, defender: UnitDefinition): Double {
        // TODO: (OPCJONALNE według konceptu) bonus dla broniącego się zależny od pola
        return if (attacker.isRanged && defender.isArmored) RANGED_VS_ARMORED_BONUS else 1.0
    }

    companion object {
        const val MIN_RANDOM_FACTOR = 0.9
        const val MAX_RANDOM_FACTOR = 1.1
        const val COUNTER_ATTACK_MULTIPLIER = 0.5
        const val RANGED_VS_ARMORED_BONUS = 2.0
        const val MINIMUM_DAMAGE = 1
    }
}
