package com.Labs2077.civgame.engine.units

/**
 * Stan konkretnej jednostki na mapie. Statystyki bazowe pobiera się z rulesetu przez definitionId
 */
data class UnitInstance(
    val instanceId: Long,
    val definitionId: String,
    val ownerId: Int,
    val position: Pair<Int, Int>,
    val currentHp: Int,
    val movementRemaining: Int,
    val hasMovedThisTurn: Boolean = false,
    val hasAttackedThisTurn: Boolean = false
) {
    init {
        require(instanceId >= 0) { "instanceId nie może być ujemne" }
        require(ownerId >= 0) { "ownerId nie może być ujemne" }
        require(currentHp >= 0) { "currentHp nie może być ujemne" }
        require(movementRemaining >= 0) { "movementRemaining nie może być ujemne" }
        require(definitionId.isNotBlank()) { "definitionId nie może być puste" }
    }

    val isAlive: Boolean get() = currentHp > 0
}

/** Tworzy nowy egzemplarz jednostki z pełnym HP i pulą ruchu na początku tury. */
fun UnitDefinition.createInstance(
    instanceId: Long,
    ownerId: Int,
    position: Pair<Int, Int>
): UnitInstance = UnitInstance(
    instanceId = instanceId,
    definitionId = id,
    ownerId = ownerId,
    position = position,
    currentHp = maxHp,
    movementRemaining = movement
)
