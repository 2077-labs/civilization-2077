package com.labs2077.civilization.core.models

import kotlin.uuid.Uuid

@JvmInline
value class UnitId(
    val value: Uuid,
)

class Unit(
    val id: UnitId = UnitId(value = Uuid.random()),
    val definition: UnitDefinition,
    val civilization: Civilization,
    var position: Hexagon,
    health: Int = definition.health,
    movementPoints: Int = definition.movement,
) {
    var health: Int = health
        set(value) {
            field = value.coerceIn(0..definition.health)
        }

    val isAlive: Boolean
        get() = health > 0

    var movementPoints: Int = movementPoints
        set(value) {
            field = value.coerceIn(0..definition.movement)
        }

    companion object
}
