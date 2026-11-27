package com.labs2077.civilization.engine.combat

import com.labs2077.civilization.core.models.Resources
import com.labs2077.civilization.core.models.UnitAbility
import com.labs2077.civilization.core.models.UnitDefinition
import com.labs2077.civilization.core.models.UnitDefinitionId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DamageCalculatorTests {

    private val calculator = DamageCalculator { 1.0 }

    @Test
    fun `given equal attack and defense then damage equals attack`() {
        val damage = calculator.attackDamage(
            unit(attack = 10, defense = 10),
            unit(attack = 10, defense = 10)
        )

        Assertions.assertEquals(10, damage)
    }

    @Test
    fun `given infantry attacking infantry then use formula and round`() {
        // 5 * 2 * 5 / (5 + 4) = 5,56
        val damage =
            calculator.attackDamage(unit(attack = 5, defense = 4), unit(attack = 5, defense = 4))

        Assertions.assertEquals(6, damage)
    }

    @Test
    fun `given titan attacking infantry then use formula and round`() {
        // 18 * 2 * 18 / (18 + 4) = 29,45
        val damage =
            calculator.attackDamage(unit(attack = 18, defense = 14), unit(attack = 5, defense = 4))

        Assertions.assertEquals(29, damage)
    }

    @Test
    fun `given random factor then scale damage`() {
        val attacker = unit(attack = 10, defense = 10)
        val defender = unit(attack = 10, defense = 10)

        Assertions.assertAll(
            {
                Assertions.assertEquals(
                    9,
                    DamageCalculator { 0.9 }.attackDamage(attacker, defender)
                )
            },
            {
                Assertions.assertEquals(
                    11,
                    DamageCalculator { 1.1 }.attackDamage(attacker, defender)
                )
            },
        )
    }

    @Test
    fun `given default random source then damage stays within random range`() {
        val attacker = unit(attack = 10, defense = 10)
        val defender = unit(attack = 10, defense = 10)
        val defaultCalculator = DamageCalculator()

        repeat(200) {
            val damage = defaultCalculator.attackDamage(attacker, defender)

            Assertions.assertTrue(damage in 9..11, "Obrażenia poza zakresem: $damage")
        }
    }

    @Test
    fun `given counter attack then deal half damage`() {
        val damage = calculator.counterAttackDamage(
            unit(attack = 10, defense = 10),
            unit(attack = 10, defense = 10)
        )

        Assertions.assertEquals(5, damage)
    }

    @Test
    fun `given ranged attacker against armored defender then double damage`() {
        val damage = calculator.attackDamage(
            unit(attack = 10, defense = 10, attackRange = 2),
            unit(attack = 10, defense = 10, isArmored = true),
        )

        Assertions.assertEquals(20, damage)
    }

    @Test
    fun `given ranged attacker against unarmored defender then no bonus`() {
        val damage = calculator.attackDamage(
            unit(attack = 10, defense = 10, attackRange = 2),
            unit(attack = 10, defense = 10),
        )

        Assertions.assertEquals(10, damage)
    }

    @Test
    fun `given melee attacker against armored defender then no bonus`() {
        val damage = calculator.attackDamage(
            unit(attack = 10, defense = 10, attackRange = 1),
            unit(attack = 10, defense = 10, isArmored = true),
        )

        Assertions.assertEquals(10, damage)
    }

    @Test
    fun `given ranged defender counter attacking armored attacker then bonus applies to counter attack`() {
        val damage = calculator.counterAttackDamage(
            unit(attack = 10, defense = 10, attackRange = 2),
            unit(attack = 10, defense = 10, isArmored = true),
        )

        Assertions.assertEquals(10, damage)
    }

    @Test
    fun `given very weak attack then deal at least one damage`() {
        val damage =
            calculator.attackDamage(unit(attack = 1, defense = 1), unit(attack = 1, defense = 100))

        Assertions.assertEquals(1, damage)
    }

    @Test
    fun `given attacker without attack then deal no damage`() {
        val damage = calculator.attackDamage(
            unit(attack = 0, defense = 1, attackRange = 0), unit(attack = 1, defense = 1),
        )

        Assertions.assertEquals(0, damage)
    }

    private fun unit(
        attack: Int,
        defense: Int,
        attackRange: Int = 1,
        isArmored: Boolean = false,
    ) = UnitDefinition(
        id = UnitDefinitionId("unit"),
        displayName = "Unit",
        attack = attack,
        attackRange = attackRange,
        defense = defense,
        isArmored = isArmored,
        health = 100,
        movement = 1.0,
        vision = 1,
        abilities = if (attackRange > 0) setOf(UnitAbility.ATTACK) else emptySet(),
        cost = Resources(),
    )
}
