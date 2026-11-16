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
    movement: Int = definition.movement,
) {
    var health: Int = 0
        set(value) {
            field = value.coerceIn(0..definition.health)
        }

    var movement: Int = 0
        set(value) {
            field = value.coerceIn(0..definition.movement)
        }

    init {
        this.health = health
        this.movement = movement
    }

    val isAlive: Boolean
        get() = health > 0
}
