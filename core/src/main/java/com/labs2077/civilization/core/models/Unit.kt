package com.labs2077.civilization.core.models

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.api.events.UnitMovedEvent
import com.labs2077.civilization.core.api.events.UnitMovementUpdatedEvent
import kotlin.uuid.Uuid

@JvmInline
value class UnitId(
    val value: Uuid,
)

class Unit(
    val id: UnitId = UnitId(value = Uuid.random()),
    val definition: UnitDefinition,
    val civilization: Civilization,
    position: Hexagon,
    health: Int = definition.health,
    movement: Int = definition.movement,
) {
    var position: Hexagon = position
        private set

    var health: Int = 0
        private set(value) {
            field = value.coerceIn(0..definition.health)
        }

    var movement: Int = 0
        private set(value) {
            field = value.coerceIn(0..definition.movement)
        }

    init {
        this.health = health
        this.movement = movement
    }

    var hasMovedThisTurn: Boolean = false
        private set

    var hasAttackedThisTurn: Boolean = false
        private set

    val isAlive: Boolean
        get() = health > 0

    fun moveTo(destination: Hexagon, cost: Int): List<Event> {
        require(destination in position.neighbors()) {
            "Miejsce docelowe znajduje się zbyt daleko jednostki: $id (cel = $destination)."
        }
        require(movement - cost >= 0) {
            "Jednostka posiada niewystarczającą ilość punków ruchu: $id (jest = $movement, wymagane = $cost)."
        }

        val source = position

        position = destination
        movement -= cost
        hasMovedThisTurn = true

        return listOf(
            UnitMovedEvent(id, source, destination),
            UnitMovementUpdatedEvent(id, movement),
        )
    }
}
