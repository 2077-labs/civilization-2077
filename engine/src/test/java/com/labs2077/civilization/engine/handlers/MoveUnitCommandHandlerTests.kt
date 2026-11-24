@file:Suppress("SameParameterValue")

package com.labs2077.civilization.engine.handlers

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.api.commands.MoveUnitCommand
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationNotPassableError
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationOccupiedError
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationOutOfBounds
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationTooFarAwayError
import com.labs2077.civilization.core.api.errors.NotEnoughMovementPointsError
import com.labs2077.civilization.core.api.errors.UnitNotFoundError
import com.labs2077.civilization.core.api.errors.UnitNotOwnedError
import com.labs2077.civilization.core.api.events.UnitMovedEvent
import com.labs2077.civilization.core.api.events.UnitMovementUpdatedEvent
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
import com.labs2077.civilization.core.models.UnitDefinition
import com.labs2077.civilization.core.models.UnitDefinitionId
import com.labs2077.civilization.engine.Context
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.opentest4j.AssertionFailedError

class MoveUnitCommandHandlerTests {

    private companion object {
        private const val TILE_MOVEMENT_COST = 2.0
        private const val INFINITE_MOVEMENT_POINTS = 999.0
        private const val MAP_WIDTH = 10
        private const val MAP_HEIGHT = 10
        private val ORIGIN_TILE = Hexagon(0, 0)
        private val ADJACENT_TILE = Hexagon(0, 1)
        private val AWAY_TILE = Hexagon(0, 2)
        private val INNER_CORNER_TILE = Hexagon(MAP_WIDTH - 1, MAP_HEIGHT - 1)
        private val OUTER_CORNER_TILE = Hexagon(MAP_WIDTH - 1, MAP_HEIGHT)
        private val UNPASSABLE_TILE = Hexagon(5, 5)
        private val NEXT_TO_UNPASSABLE_TILE = UNPASSABLE_TILE.copy(q = UNPASSABLE_TILE.q + 1)
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

    private val unpassableTerrain = passableTerrain.copy(
        id = TerrainDefinitionId("unpassable-terrain"),
        displayName = "Unpassable Terrain",
        passable = false,
    )

    private val unitDefinition = UnitDefinition(
        id = UnitDefinitionId("default-unit"),
        displayName = "Default Unit",
        attack = 1,
        attackRange = 0,
        defense = 1,
        health = 1,
        movement = INFINITE_MOVEMENT_POINTS,
        vision = 1,
        cost = Resources(),
    )

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
                val definition = if (position == UNPASSABLE_TILE) {
                    unpassableTerrain
                } else {
                    passableTerrain
                }
                Terrain(definition, position)
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

    @Nested
    @DisplayName("Unit validation")
    inner class UnitValidation {
        @Test
        fun `given unit not in any civilization then return UnitNotFoundError`() {
            val unit = createUnit()

            val error = assertMoveFailsWith<UnitNotFoundError>(unit, ADJACENT_TILE)

            Assertions.assertEquals(unit.id, error.unitId)
        }

        @Test
        fun `given unit controlled by another civilization then return UnitNotOwnedError`() {
            val unit = createAndAddUnit(civilization = enemyCivilization)

            val error = assertMoveFailsWith<UnitNotOwnedError>(unit, ADJACENT_TILE)

            Assertions.assertEquals(unit.id, error.unitId)
        }

        @Test
        fun `given unit has not enough movement points then return NotEnoughMovementPointsError`() {
            val unit = createAndAddUnit(movement = TILE_MOVEMENT_COST - 1, position = ORIGIN_TILE)

            val error = assertMoveFailsWith<NotEnoughMovementPointsError>(unit, ADJACENT_TILE)

            Assertions.assertAll(
                { Assertions.assertEquals(unit.id, error.unitId) },
                { Assertions.assertEquals(TILE_MOVEMENT_COST, error.requiredMovementPoints) },
            )
        }

    }

    @Nested
    @DisplayName("Destination validation")
    inner class DestinationValidation {
        @Test
        fun `given unit is more than one tile from destination then return MoveUnitDestinationTooFarAwayError`() {
            val unit = createAndAddUnit(position = ORIGIN_TILE)

            val error = assertMoveFailsWith<MoveUnitDestinationTooFarAwayError>(unit, AWAY_TILE)

            Assertions.assertAll(
                { Assertions.assertEquals(unit.id, error.unitId) },
                { Assertions.assertEquals(AWAY_TILE, error.destination) },
            )
        }

        @Test
        fun `given destination is outside map then return MoveUnitDestinationOutOfBounds`() {
            val unit = createAndAddUnit(position = INNER_CORNER_TILE)

            val error = assertMoveFailsWith<MoveUnitDestinationOutOfBounds>(
                unit, OUTER_CORNER_TILE,
            )

            Assertions.assertAll(
                { Assertions.assertEquals(unit.id, error.unitId) },
                { Assertions.assertEquals(OUTER_CORNER_TILE, error.destination) },
            )
        }

        @Test
        fun `given destination is not passable then return MoveUnitDestinationNotPassableError`() {
            val unit = createAndAddUnit(position = NEXT_TO_UNPASSABLE_TILE)

            val error = assertMoveFailsWith<MoveUnitDestinationNotPassableError>(
                unit, UNPASSABLE_TILE,
            )

            Assertions.assertAll(
                { Assertions.assertEquals(unit.id, error.unitId) },
                { Assertions.assertEquals(UNPASSABLE_TILE, error.destination) },
            )
        }

        @Test
        fun `given another unit occupies destination then return MoveUnitDestinationOccupiedError`() {
            val moving = createAndAddUnit(position = ORIGIN_TILE)
            val blocking = createAndAddUnit(position = ADJACENT_TILE)

            val error = assertMoveFailsWith<MoveUnitDestinationOccupiedError>(moving, ADJACENT_TILE)

            assertAll(
                { Assertions.assertEquals(moving.id, error.unitId) },
                { Assertions.assertEquals(ADJACENT_TILE, error.destination) },
                { Assertions.assertEquals(listOf(blocking), error.units) },
            )
        }
    }

    @Nested
    @DisplayName("Valid move")
    inner class ValidMove {

        @Test
        fun `then emit UnitMovedEvent`() {
            val unit = createAndAddUnit(position = ORIGIN_TILE)

            val events = assertMoveSucceeds(unit, ADJACENT_TILE)

            val expected = UnitMovedEvent(unit.id, ORIGIN_TILE, ADJACENT_TILE)
            Assertions.assertTrue(expected in events)
        }

        @Test
        fun `then deduct movement cost and emit UnitMovementUpdatedEvent`() {
            val startingMovement = TILE_MOVEMENT_COST + 1
            val unit = createAndAddUnit(position = ORIGIN_TILE, movement = startingMovement)

            val events = assertMoveSucceeds(unit, ADJACENT_TILE)

            val expected = UnitMovementUpdatedEvent(unit.id, startingMovement - TILE_MOVEMENT_COST)
            Assertions.assertTrue(expected in events)
        }
    }

    private fun createUnit(
        civilization: Civilization = ownedCivilization,
        position: Hexagon = ORIGIN_TILE,
        movement: Double = INFINITE_MOVEMENT_POINTS,
    ) = Unit(
        definition = unitDefinition,
        civilization = civilization,
        position = position,
        movement = movement,
    )

    private fun createAndAddUnit(
        civilization: Civilization = ownedCivilization,
        position: Hexagon = ORIGIN_TILE,
        movement: Double = INFINITE_MOVEMENT_POINTS,
    ) = createUnit(
        civilization = civilization,
        position = position,
        movement = movement,
    ).also {
        civilization.add(it)
    }

    private inline fun <reified E : Error> assertMoveFailsWith(
        unit: Unit, destination: Hexagon
    ): E {
        val result = handle(unit, destination)

        if (result !is Result.Error) {
            throw AssertionFailedError("Move should fail.", Error::class.simpleName, result)
        }
        return Assertions.assertInstanceOf(E::class.java, result.error)
    }

    private fun assertMoveSucceeds(unit: Unit, destination: Hexagon): List<Event> {
        val result = handle(unit, destination)

        if (result !is Result.Success) {
            throw AssertionFailedError("Move should succeed.", Error::class.simpleName, result)
        }
        return result.value
    }

    private fun handle(unit: Unit, destination: Hexagon): Result<List<Event>, Error> {
        val handler = MoveUnitCommandHandler(context, state)
        val command = MoveUnitCommand(unit.id, destination)

        return handler.handle(command)
    }
}
