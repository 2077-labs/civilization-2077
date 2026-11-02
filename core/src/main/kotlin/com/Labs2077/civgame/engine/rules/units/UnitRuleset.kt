package com.Labs2077.civgame.engine.rules.units

import com.Labs2077.civgame.engine.rules.AbstractRuleset
import com.Labs2077.civgame.engine.rules.UnitId
import com.Labs2077.civgame.engine.units.UnitAbility
import com.Labs2077.civgame.engine.units.UnitDefinition
import kotlin.collections.forEach

/**
 * Ruleset jednostek, który zawiera wszystkie definicje jednostek w grze.
 * Waliduje definicje jednostek przy tworzeniu.
 */
class UnitRuleset private constructor(
    schemaVersion: Int,
    units: List<UnitDefinition>
) : AbstractRuleset<UnitId, UnitDefinition>(
    schemaVersion = schemaVersion,
    definitions = units,
    typeName = "units"
) {
    val units: List<UnitDefinition>
        get() = definitions

    companion object {
        fun fromFile(file: UnitRulesetFile): UnitRuleset {
            require(file.schemaVersion == 1) {
                "Nieobsługiwana wersja units.json: " +
                    file.schemaVersion
            }

            file.units.forEach(::validateDefinition)

            return UnitRuleset(file.schemaVersion, file.units)
        }

        private fun validateDefinition(unit: UnitDefinition) {
            require(unit.id.value.matches(Regex("[a-z][a-z0-9_]*"))) {
                "Niepoprawne id jednostki '${unit.id.value}' — użyj małych liter, cyfr i '_'"
            }
            require(unit.displayName.isNotBlank()) { "${unit.id.value}: displayName nie może być puste" }
            require(unit.maxHp > 0) { "${unit.id.value}: maxHp musi być > 0" }
            require(unit.attack >= 0) { "${unit.id.value}: attack nie może być ujemne" }
            require(unit.defense >= 0) { "${unit.id.value}: defense nie może być ujemne" }
            require(unit.movement >= 0) { "${unit.id.value}: movement nie może być ujemne" }
            require(unit.vision >= 0) { "${unit.id.value}: vision nie może być ujemne" }
            require(unit.attackRange >= 1) { "${unit.id.value}: attackRange musi być >= 1" }
            require(unit.cost.isNonNegative()) { "${unit.id.value}: koszt nie może być ujemny" }
            require(unit.upkeep.isNonNegative()) { "${unit.id.value}: utrzymanie nie może być ujemne" }
            require(unit.requiredBuildings.all { it.value.isNotBlank() }) {
                "${unit.id.value}: wymagania budynków nie mogą być puste"
            }
            require(unit.limitPerPlayer == null || unit.limitPerPlayer > 0) {
                "${unit.id.value}: limitPerPlayer musi być dodatni albo null"
            }

            val melee = UnitAbility.ATTACK_MELEE in unit.abilities
            val ranged = UnitAbility.ATTACK_RANGED in unit.abilities
            require(!(melee && ranged)) {
                "${unit.id.value}: wybierz ATTACK_MELEE albo ATTACK_RANGED; obecny model ma jeden attackRange"
            }
            require((unit.attack > 0) == (melee || ranged)) {
                "${unit.id.value}: attack > 0 musi odpowiadać jednej zdolności ataku, a attack = 0 żadnej"
            }
            if (melee) require(unit.attackRange == 1) {
                "${unit.id.value}: atak wręcz musi mieć attackRange = 1"
            }
            if (ranged) require(unit.attackRange > 1) {
                "${unit.id.value}: atak dystansowy musi mieć attackRange > 1"
            }
            require(
                UnitAbility.CANNOT_ATTACK_AFTER_MOVING !in unit.abilities || melee || ranged
            ) { "${unit.id.value}: CANNOT_ATTACK_AFTER_MOVING wymaga zdolności ataku" }
        }
    }
}
