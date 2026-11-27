package com.labs2077.civilization.engine.handlers

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
import com.labs2077.civilization.core.api.events.UnitDestroyedEvent
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.GameState
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitAbility
import com.labs2077.civilization.engine.Context
import com.labs2077.civilization.engine.combat.DamageCalculator
import com.labs2077.civilization.engine.dispatch.CommandHandler
import com.labs2077.civilization.engine.fog_of_war.UnitVisibilityProvider
import com.labs2077.civilization.engine.fog_of_war.VisibilityProvider

internal class AttackUnitCommandHandler(
    val context: Context,
    val state: GameState,
    private val visibility: VisibilityProvider = UnitVisibilityProvider,
    private val damageCalculator: DamageCalculator = DamageCalculator(),
) : CommandHandler<AttackUnitCommand> {

    override fun handle(command: AttackUnitCommand): Result<List<Event>, Error> {
        val attacker = state.findUnitById(command.unitId)
            ?: return Result.Error(UnitNotFoundError(command.unitId))
        val target = state.findUnitById(command.targetId)
            ?: return Result.Error(UnitNotFoundError(command.targetId))

        validate(attacker, target)?.let { error ->
            return Result.Error(error)
        }

        val events = mutableListOf<Event>()

        events.add(UnitAttackedEvent(attacker.id, target.id))
        attacker.markAttacked()

        events.addAll(
            target.takeDamage(
                damageCalculator.attackDamage(
                    attacker.definition,
                    target.definition
                )
            )
        )

        if (!target.isAlive) {
            events.add(destroy(target, destroyedBy = attacker))
            return Result.Success(events)
        }

        if (canCounterAttack(target, attacker)) {
            events.add(UnitCounterAttackedEvent(target.id, attacker.id))
            events.addAll(
                attacker.takeDamage(
                    damageCalculator.counterAttackDamage(
                        target.definition,
                        attacker.definition
                    )
                )
            )

            if (!attacker.isAlive) {
                events.add(destroy(attacker, destroyedBy = target))
            }
        }

        return Result.Success(events)
    }

    private fun validate(attacker: Unit, target: Unit): Error? {
        // Gracz musi być w stanie kontrolować jednostkę atakującą.
        if (attacker.civilization.id != context.civilization.id) {
            return UnitNotOwnedError(attacker.id)
        }

        // Nie można atakować własnych jednostek.
        if (target.civilization.id == attacker.civilization.id) {
            return AttackUnitTargetIsFriendlyError(attacker.id, target.id)
        }

        // Jednostka musi posiadać zdolność ataku (np. osadnik nie może atakować).
        val definition = attacker.definition

        if (UnitAbility.ATTACK !in definition.abilities) {
            return AttackUnitCannotAttackError(attacker.id)
        }

        // Jedna jednostka może wykonać jeden atak na turę.
        if (attacker.hasAttackedThisTurn) {
            return AttackUnitAlreadyAttackedError(attacker.id)
        }

        // Niektóre jednostki (np. artyleria rakietowa) nie mogą atakować po ruchu.
        if (attacker.hasMovedThisTurn && !definition.canAttackAfterMoving) {
            return AttackUnitCannotAttackAfterMovingError(attacker.id)
        }

        // Cel musi znajdować się w zasięgu ataku.
        val distance = attacker.position.distanceTo(target.position)

        if (distance > definition.attackRange) {
            return AttackUnitTargetOutOfRangeError(
                attacker.id,
                target.id,
                distance,
                definition.attackRange
            )
        }

        // Cel musi być widoczny i odkryty.
        if (!visibility.isVisible(attacker.civilization, target.position)) {
            return AttackUnitTargetNotVisibleError(attacker.id, target.id)
        }

        return null
    }

    /**
     * Broniący się kontratakuje, jeśli przeżył, potrafi atakować i atakujący znajduje się w jego zasięgu.
     * Dzięki temu atak z dystansu nie dostaje kontrataku od jednostek, które tak daleko nie sięgają,
     * a walka w zwarciu zawsze wywołuje kontratak uzbrojonego obrońcy.
     */
    private fun canCounterAttack(defender: Unit, attacker: Unit): Boolean {
        val definition = defender.definition

        return defender.isAlive
                && UnitAbility.ATTACK in definition.abilities
                && defender.position.distanceTo(attacker.position) <= definition.attackRange
    }

    private fun destroy(unit: Unit, destroyedBy: Unit): UnitDestroyedEvent {
        unit.civilization.remove(unit.id)

        return UnitDestroyedEvent(unit.id, unit.position, destroyedBy.id)
    }
}
