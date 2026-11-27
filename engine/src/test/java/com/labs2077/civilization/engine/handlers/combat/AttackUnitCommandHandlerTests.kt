@file:Suppress("SameParameterValue")

package com.labs2077.civilization.engine.handlers.combat

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.api.commands.AttackUnitCommand
import com.labs2077.civilization.core.api.errors.AttackUnitAlreadyAttackedError
import com.labs2077.civilization.core.api.errors.AttackUnitCannotAttackAfterMovingError
import com.labs2077.civilization.core.api.errors.AttackUnitCannotAttackError
import com.labs2077.civilization.core.api.errors.AttackUnitTargetIsFriendlyError
import com.labs2077.civilization.core.api.errors.AttackUnitTargetNotVisibleError
import com.labs2077.civilization.core.api.errors.AttackUnitTargetOutOfRangeError
import com.labs2077.civilization.core.api.errors.UnitNotFoundError
import com.labs2077.civilization.core.api.errors.UnitNotOwnedError
import com.labs2077.civilization.core.api.events.UnitAttackedEvent
import com.labs2077.civilization.core.api.events.UnitCounterAttackedEvent
import com.labs2077.civilization.core.api.events.UnitDamagedEvent
import com.labs2077.civilization.core.api.events.UnitDestroyedEvent
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.Color
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.GameState
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.HexagonGrid
import com.labs2077.civilization.core.models.Player
import com.labs2077.civilization.core.models.Resources
import com.labs2077.civilization.core.models.Terrain
import com.labs2077.civilization.core.models.TerrainDefinition
import com.labs2077.civilization.core.models.TerrainDefinitionId
import com.labs2077.civilization.core.models.TerrainMap
import com.labs2077.civilization.core.models.TerrainProduction
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitAbility
import com.labs2077.civilization.core.models.UnitDefinition
import com.labs2077.civilization.core.models.UnitDefinitionId
import com.labs2077.civilization.engine.Context
import com.labs2077.civilization.engine.combat.DamageCalculator
import com.labs2077.civilization.engine.fog_of_war.VisibilityProvider
import com.labs2077.civilization.engine.handlers.AttackUnitCommandHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.opentest4j.AssertionFailedError

/**
 * Przy współczynniku losowości równym 1,0 oraz ataku i obronie równych 10 atak zadaje dokładnie 10 obrażeń,
 * a kontratak 5, co upraszcza asercje.
 */
class AttackUnitCommandHandlerTests {

    private companion object {
        private const val TILE_MOVEMENT_COST = 2.0
        private const val MAP_WIDTH = 10
        private const val MAP_HEIGHT = 10
        private const val FULL_HEALTH = 100
        private const val ATTACK_DAMAGE = 10
        private const val COUNTER_ATTACK_DAMAGE = 5

        private val ORIGIN_TILE = Hexagon(0, 0)
        private val ADJACENT_TILE = Hexagon(0, 1)
        private val DISTANCE_2_TILE = Hexagon(0, 2)
        private val DISTANCE_3_TILE = Hexagon(0, 3)
    }

    private val passableTerrain = TerrainDefinition(
        id = TerrainDefinitionId("passable-terrain"),
        displayName = "Passable Terrain",
        passable = true,
        movementCost = TILE_MOVEMENT_COST,
        production = TerrainProduction.Fixed(
            resources = Resources(),
        )
    )

    private val meleeDefinition = createDefinition(id = "melee", attackRange = 1)
    private val rangedDefinition = createDefinition(id = "ranged", attackRange = 2)
    private val artilleryDefinition = createDefinition(
        id = "artillery", attackRange = 3, canAttackAfterMoving = false,
    )
    private val armoredDefinition =
        createDefinition(id = "armored", attackRange = 1, isArmored = true)
    private val passiveDefinition = createDefinition(id = "passive", attackRange = 0, attack = 0)

    private val ownedCivilization = createCivilization()
    private val enemyCivilization = createCivilization()

    private fun createCivilization() = Civilization(
        player = Player(),
        color = Color(0x0),
    )

    private val context = Context(
        player = ownedCivilization.player,
        civilization = ownedCivilization,
    )

    private val state = GameState(
        map = TerrainMap(
            tiles = HexagonGrid.create(MAP_WIDTH, MAP_HEIGHT) { position ->
                Terrain(passableTerrain, position)
            }),
        players = listOf(
            ownedCivilization.player,
            enemyCivilization.player,
        ),
        civilizations = listOf(
            ownedCivilization,
            enemyCivilization,
        ),
    )

    /** Tymczasowo ustawiana widoczność celów; domyślnie wszystko jest widoczne. */
    private var targetsVisible = true

    @Nested
    @DisplayName("Validation")
    inner class Validation {
        @Test
        fun `given attacker or target not in any civilization then return UnitNotFoundError`() {
            val known = createAndAddUnit(meleeDefinition, ownedCivilization, ORIGIN_TILE)
            val unknown = createUnit(meleeDefinition, enemyCivilization, ADJACENT_TILE)

            val attackerError = assertAttackFailsWith<UnitNotFoundError>(unknown, known)
            val targetError = assertAttackFailsWith<UnitNotFoundError>(known, unknown)

            assertAll(
                { Assertions.assertEquals(unknown.id, attackerError.unitId) },
                { Assertions.assertEquals(unknown.id, targetError.unitId) },
            )
        }

        @Test
        fun `given attacker controlled by another civilization then return UnitNotOwnedError`() {
            val attacker = createAndAddUnit(meleeDefinition, enemyCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, ownedCivilization, ADJACENT_TILE)

            val error = assertAttackFailsWith<UnitNotOwnedError>(attacker, target)

            Assertions.assertEquals(attacker.id, error.unitId)
        }

        @Test
        fun `given target belongs to the same civilization then return AttackUnitTargetIsFriendlyError`() {
            val attacker = createAndAddUnit(meleeDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, ownedCivilization, ADJACENT_TILE)

            val error = assertAttackFailsWith<AttackUnitTargetIsFriendlyError>(attacker, target)

            assertAll(
                { Assertions.assertEquals(attacker.id, error.unitId) },
                { Assertions.assertEquals(target.id, error.targetId) },
            )
        }

        @Test
        fun `given attacker without attack ability then return AttackUnitCannotAttackError`() {
            val attacker = createAndAddUnit(passiveDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, ADJACENT_TILE)

            val error = assertAttackFailsWith<AttackUnitCannotAttackError>(attacker, target)

            Assertions.assertEquals(attacker.id, error.unitId)
        }

        @Test
        fun `given attacker already attacked this turn then return AttackUnitAlreadyAttackedError`() {
            val attacker = createAndAddUnit(meleeDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, ADJACENT_TILE)
            assertAttackSucceeds(attacker, target)

            val error = assertAttackFailsWith<AttackUnitAlreadyAttackedError>(attacker, target)

            Assertions.assertEquals(attacker.id, error.unitId)
        }

        @Test
        fun `given unit that cannot attack after moving has moved then return AttackUnitCannotAttackAfterMovingError`() {
            val attacker = createAndAddUnit(artilleryDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, DISTANCE_3_TILE)
            attacker.moveTo(ADJACENT_TILE, TILE_MOVEMENT_COST)

            val error =
                assertAttackFailsWith<AttackUnitCannotAttackAfterMovingError>(attacker, target)

            Assertions.assertEquals(attacker.id, error.unitId)
        }

        @Test
        fun `given target beyond attack range then return AttackUnitTargetOutOfRangeError`() {
            val attacker = createAndAddUnit(meleeDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, DISTANCE_2_TILE)

            val error = assertAttackFailsWith<AttackUnitTargetOutOfRangeError>(attacker, target)

            assertAll(
                { Assertions.assertEquals(attacker.id, error.unitId) },
                { Assertions.assertEquals(target.id, error.targetId) },
                { Assertions.assertEquals(2, error.distance) },
                { Assertions.assertEquals(1, error.range) },
            )
        }

        @Test
        fun `given target not visible then return AttackUnitTargetNotVisibleError`() {
            targetsVisible = false
            val attacker = createAndAddUnit(meleeDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, ADJACENT_TILE)

            val error = assertAttackFailsWith<AttackUnitTargetNotVisibleError>(attacker, target)

            assertAll(
                { Assertions.assertEquals(attacker.id, error.unitId) },
                { Assertions.assertEquals(target.id, error.targetId) },
            )
        }
    }

    @Nested
    @DisplayName("Valid attack")
    inner class ValidAttack {
        @Test
        fun `given melee attack then emit events in order and update units`() {
            val attacker = createAndAddUnit(meleeDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, ADJACENT_TILE)

            val events = assertAttackSucceeds(attacker, target)

            assertAll(
                {
                    Assertions.assertEquals(
                        listOf<Event>(
                            UnitAttackedEvent(attacker.id, target.id),
                            UnitDamagedEvent(target.id, ATTACK_DAMAGE, FULL_HEALTH - ATTACK_DAMAGE),
                            UnitCounterAttackedEvent(target.id, attacker.id),
                            UnitDamagedEvent(
                                attacker.id,
                                COUNTER_ATTACK_DAMAGE,
                                FULL_HEALTH - COUNTER_ATTACK_DAMAGE
                            ),
                        ),
                        events,
                    )
                },
                { Assertions.assertEquals(FULL_HEALTH - COUNTER_ATTACK_DAMAGE, attacker.health) },
                { Assertions.assertEquals(FULL_HEALTH - ATTACK_DAMAGE, target.health) },
                { Assertions.assertTrue(attacker.hasAttackedThisTurn) },
                { Assertions.assertFalse(target.hasAttackedThisTurn) },
            )
        }

        @Test
        fun `given ranged attack against melee unit then no counter attack`() {
            val attacker = createAndAddUnit(rangedDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, DISTANCE_2_TILE)

            val events = assertAttackSucceeds(attacker, target)

            assertAll(
                {
                    Assertions.assertEquals(
                        listOf<Event>(
                            UnitAttackedEvent(attacker.id, target.id),
                            UnitDamagedEvent(target.id, ATTACK_DAMAGE, FULL_HEALTH - ATTACK_DAMAGE),
                        ),
                        events,
                    )
                },
                { Assertions.assertEquals(FULL_HEALTH, attacker.health) },
            )
        }

        @Test
        fun `given ranged attacker against armored target then deal double damage`() {
            val attacker = createAndAddUnit(rangedDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(armoredDefinition, enemyCivilization, DISTANCE_2_TILE)

            val events = assertAttackSucceeds(attacker, target)

            val expected =
                UnitDamagedEvent(target.id, ATTACK_DAMAGE * 2, FULL_HEALTH - ATTACK_DAMAGE * 2)
            Assertions.assertTrue(expected in events)
        }

        @Test
        fun `given target at the very edge of range then attack succeeds`() {
            val attacker = createAndAddUnit(artilleryDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, DISTANCE_3_TILE)

            val events = assertAttackSucceeds(attacker, target)

            Assertions.assertTrue(UnitAttackedEvent(attacker.id, target.id) in events)
        }
    }

    @Nested
    @DisplayName("Unit destruction")
    inner class UnitDestruction {
        @Test
        fun `given lethal attack then destroy target without counter attack`() {
            val attacker = createAndAddUnit(meleeDefinition, ownedCivilization, ORIGIN_TILE)
            val target = createAndAddUnit(
                meleeDefinition, enemyCivilization, ADJACENT_TILE, health = ATTACK_DAMAGE,
            )

            val events = assertAttackSucceeds(attacker, target)

            assertAll(
                {
                    Assertions.assertEquals(
                        listOf<Event>(
                            UnitAttackedEvent(attacker.id, target.id),
                            UnitDamagedEvent(target.id, ATTACK_DAMAGE, 0),
                            UnitDestroyedEvent(target.id, ADJACENT_TILE, attacker.id),
                        ),
                        events,
                    )
                },
                { Assertions.assertFalse(target in enemyCivilization.units) },
                { Assertions.assertEquals(FULL_HEALTH, attacker.health) },
            )
        }

        @Test
        fun `given lethal counter attack then destroy attacker`() {
            val attacker = createAndAddUnit(
                meleeDefinition, ownedCivilization, ORIGIN_TILE, health = COUNTER_ATTACK_DAMAGE,
            )
            val target = createAndAddUnit(meleeDefinition, enemyCivilization, ADJACENT_TILE)

            val events = assertAttackSucceeds(attacker, target)

            assertAll(
                {
                    Assertions.assertEquals(
                        listOf<Event>(
                            UnitAttackedEvent(attacker.id, target.id),
                            UnitDamagedEvent(target.id, ATTACK_DAMAGE, FULL_HEALTH - ATTACK_DAMAGE),
                            UnitCounterAttackedEvent(target.id, attacker.id),
                            UnitDamagedEvent(attacker.id, COUNTER_ATTACK_DAMAGE, 0),
                            UnitDestroyedEvent(attacker.id, ORIGIN_TILE, target.id),
                        ),
                        events,
                    )
                },
                { Assertions.assertFalse(attacker in ownedCivilization.units) },
                { Assertions.assertTrue(target in enemyCivilization.units) },
            )
        }
    }

    private fun createDefinition(
        id: String,
        attackRange: Int,
        attack: Int = 10,
        defense: Int = 10,
        isArmored: Boolean = false,
        canAttackAfterMoving: Boolean = true,
    ) = UnitDefinition(
        id = UnitDefinitionId(id),
        displayName = id,
        attack = attack,
        attackRange = attackRange,
        canAttackAfterMoving = canAttackAfterMoving,
        defense = defense,
        isArmored = isArmored,
        health = FULL_HEALTH,
        movement = 10.0,
        vision = 1,
        abilities = if (attackRange > 0) setOf(UnitAbility.ATTACK) else emptySet(),
        cost = Resources(),
    )

    private fun createUnit(
        definition: UnitDefinition,
        civilization: Civilization,
        position: Hexagon,
        health: Int = definition.health,
    ) = Unit(
        definition = definition,
        civilization = civilization,
        position = position,
        health = health,
    )

    private fun createAndAddUnit(
        definition: UnitDefinition,
        civilization: Civilization,
        position: Hexagon,
        health: Int = definition.health,
    ) = createUnit(definition, civilization, position, health).also {
        civilization.add(it)
    }

    private inline fun <reified E : Error> assertAttackFailsWith(attacker: Unit, target: Unit): E {
        val result = handle(attacker, target)

        if (result !is Result.Error) {
            throw AssertionFailedError("Attack should fail.", Error::class.simpleName, result)
        }
        return Assertions.assertInstanceOf(E::class.java, result.error)
    }

    private fun assertAttackSucceeds(attacker: Unit, target: Unit): List<Event> {
        val result = handle(attacker, target)

        if (result !is Result.Success) {
            throw AssertionFailedError("Attack should succeed.", Error::class.simpleName, result)
        }
        return result.value
    }

    private fun handle(attacker: Unit, target: Unit): Result<List<Event>, Error> {
        val handler = AttackUnitCommandHandler(
            context = context,
            state = state,
            visibility = VisibilityProvider { _, _ -> targetsVisible },
            damageCalculator = DamageCalculator { 1.0 },
        )
        val command = AttackUnitCommand(attacker.id, target.id)

        return handler.handle(command)
    }
}
