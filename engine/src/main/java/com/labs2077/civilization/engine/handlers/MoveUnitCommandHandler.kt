package com.labs2077.civilization.engine.handlers

import com.labs2077.civilization.core.api.commands.MoveUnitCommand
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationOccupiedError
import com.labs2077.civilization.core.api.errors.MoveUnitDestinationTooFarAwayError
import com.labs2077.civilization.core.api.errors.UnitNotFoundError
import com.labs2077.civilization.core.api.errors.UnitNotOwnedError
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitId
import com.labs2077.civilization.engine.Context
import com.labs2077.civilization.engine.State
import com.labs2077.civilization.engine.dispatch.BaseCommandHandler

internal class MoveUnitCommandHandler(context: Context, state: State) :
    BaseCommandHandler<MoveUnitCommand>(context, state) {

    override fun execute(command: MoveUnitCommand): Error? {
        val unit = state.findUnitById(command.unitId) //
            ?: return UnitNotFoundError(command.unitId)

        validate(unit, command.destination)?.let { error ->
            return error
        }

        val movementCost = 0  // TODO: Pobrać z terenu koszt ruchu.
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
        // Jednostka musi mieć wystarczającą ilość punktów ruchu.
//        if (false) {
//            // TODO: Sprawdzić, czy jednostka ma wystarczającą ilość punktów ruchu.
//            return NotEnoughMovementPointsError(unit.id, 0)
//        }

        // Jednostka nie może wejść na nieosiągalne pole,
        // ani kolidować z innymi jednostkami po wykonaniu ruchu.
//        if (false) {
//            // TODO: Sprawdzić, czy pole jest osiągalne.
//            return MoveUnitDestinationNotPassableError(unit.id, terrain)
//        }
        if (state.findUnitsByPosition(destination)
                .isNotEmpty()
        ) {
            return MoveUnitDestinationOccupiedError(unit.id, destination)
        }

        return null
    }
}

internal fun State.findUnitById(unitId: UnitId): Unit? {
    return civilizations.firstNotNullOfOrNull { civilization ->
        civilization.units.firstOrNull { it.id == unitId }
    }
}

internal fun State.findUnitsByPosition(position: Hexagon): List<Unit> {
    val result = mutableListOf<Unit>()
    civilizations.forEach { civilization ->
        civilization.units.filterTo(result) { it.position == position }
    }
    return result
}
