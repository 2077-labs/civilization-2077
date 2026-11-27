package com.labs2077.civilization.core.models

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.api.events.UnitDamagedEvent
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
    movement: Double = definition.movement,
) {
    var position: Hexagon = position
        private set

    var health: Int = 0
        private set(value) {
            field = value.coerceIn(0..definition.health)
        }

    var movement: Double = 0.0
        private set(value) {
            field = value.coerceIn(0.0..definition.movement)
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

    /**
     * Zadaje jednostce obrażenia. Punkty życia nie spadają poniżej zera,
     * a zdarzenie zawiera faktycznie zadane obrażenia.
     *
     * !!!Usunięcie zniszczonej jednostki z cywilizacji należy do obsługi polecenia!!!
     */
    fun takeDamage(damage: Int): List<Event> {
        require(damage >= 0) {
            "Obrażenia muszą być nieujemne: $damage."
        }
        require(isAlive) {
            "Jednostka została już zniszczona: $id."
        }

        val healthBefore = health

        health -= damage

        return listOf(
            UnitDamagedEvent(id, healthBefore - health, health),
        )
    }

    /** Oznacza, że jednostka wykonała w tej turze atak. */
    fun markAttacked() {
        hasAttackedThisTurn = true
    }

    fun moveTo(destination: Hexagon, cost: Double): List<Event> {
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
