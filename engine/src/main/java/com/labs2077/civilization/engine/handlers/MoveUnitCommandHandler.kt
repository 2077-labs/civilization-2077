package com.labs2077.civilization.engine.handlers

import com.labs2077.civilization.core.api.commands.MoveUnitCommand
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationNotPassableError
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationOccupiedError
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationOutOfBounds
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationTooFarAwayError
import com.labs2077.civilization.core.api.errors.NotEnoughMovementPointsError
import com.labs2077.civilization.core.api.errors.UnitNotFoundError
import com.labs2077.civilization.core.api.errors.UnitNotOwnedError
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.GameState
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitId
import com.labs2077.civilization.engine.Context
import com.labs2077.civilization.engine.dispatch.BaseCommandHandler

internal class MoveUnitCommandHandler(context: Context, state: GameState) :
    BaseCommandHandler<MoveUnitCommand>(context, state) {

    override fun execute(command: MoveUnitCommand): Error? {
        val unit = state.findUnitById(command.unitId) //
            ?: return UnitNotFoundError(command.unitId)

        validate(unit, command.destination)?.let { error ->
            return error
        }

        val terrain = state.map.tiles[command.destination]
        val movementCost = terrain.definition.movementCost

        emitAll(unit.moveTo(command.destination, movementCost))

        return null
    }

    private fun validate(unit: Unit, destination: Hexagon): Error? {
        // Gracz musi być w stanie kontrolować jednostkę.
        if (unit.civilization.id != context.civilization.id) {
            return UnitNotOwnedError(unit.id)
        }

        // Zakładamy, że jednostka może wykonywać ruch tylko o jedno pole w wybranym kierunku,
        // ale tylko wtedy, gdy ma wystarczającą ilość punktów ruchu.
        if (destination !in unit.position.neighbors()) {
            return MoveUnitDestinationTooFarAwayError(unit.id, destination)
        }

        // Jednostka nie może wejść na nieosiągalne pole
        // oraz musi posiadać wystarczającą ilość punktów ruchu.
        val terrainMap = state.map.tiles

        if (!terrainMap.contains(destination)) {
            return MoveUnitDestinationOutOfBounds(unit.id, destination)
        }

        val terrain = terrainMap[destination].definition

        if (terrain.passable) {
            return MoveUnitDestinationNotPassableError(unit.id, destination, terrain.id)
        }
        if (unit.movement < terrain.movementCost) {
            return NotEnoughMovementPointsError(unit.id, terrain.movementCost)
        }

        // Jednostka nie może kolidować z innymi jednostkami po wykonaniu ruchu.
        if (state.findUnitsByPosition(destination)
                .isNotEmpty()
        ) {
            return MoveUnitDestinationOccupiedError(unit.id, destination)
        }

        return null
    }
}

internal fun GameState.findUnitById(unitId: UnitId): Unit? {
    return civilizations.firstNotNullOfOrNull { civilization ->
        civilization.units.firstOrNull { it.id == unitId }
    }
}

internal fun GameState.findUnitsByPosition(position: Hexagon): List<Unit> {
    val result = mutableListOf<Unit>()
    civilizations.forEach { civilization ->
        civilization.units.filterTo(result) { it.position == position }
    }
    return result
}
