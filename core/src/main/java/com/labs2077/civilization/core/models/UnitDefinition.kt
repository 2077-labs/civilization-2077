package com.labs2077.civilization.core.models

@JvmInline
value class UnitDefinitionId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) {
            "Identyfikator definicji jednostki nie może być pusty."
        }
    }
}

data class UnitDefinition(
    val id: UnitDefinitionId,
    val displayName: String,

    val attack: Int,
    /** Zasięg ataku określa rodzaj walczącej jednostki i wynosi 1 dla jednostek walczących wręcz, &gt;1 dla jednostek dystansowych lub 0 dla jednostek nieposiadających zdolności ataku. */
    val attackRange: Int = 0,
    /** Niektóre jednostki (np. artyleria rakietowa) nie mogą atakować w turze, w której się wcześniej poruszyły. */
    val canAttackAfterMoving: Boolean = true,
    val defense: Int,

    val health: Int,
    val movement: Int,
    val vision: Int,

    val abilities: Set<UnitAbility> = emptySet(),

    val cost: Resources,
    /** Koszt utrzymania ponoszony przez cywilizację na początku każdej tury. */
    val upkeep: CivilizationResources,
    /** Lista rodzajów budynków, które są wymagane do odblokowania rekrutacji jednostki danego typu. */
    val requiredBuildingTypeIds: Set<BuildingDefinitionId> = emptySet(),
    /** Limit jednostek danego typu w jednej cywilizacji. Wartość `null` oznacza brak limitu. */
    val limitPerCivilization: Int? = null,
) {
    init {
        require(displayName.isNotBlank()) {
            "Nazwa jednostki (displayName) nie może być pusta: $displayName."
        }
        require(attack >= 0) {
            "Siła ataku (attack) musi być nieujemna: $attack."
        }
        if (UnitAbility.ATTACK in abilities) {
            require(attackRange >= 1) {
                "Zasięg ataku (attackRange) dla jednostki posiadającej zdolność ataku musi być dodatni: $attackRange."
            }
        } else {
            require(attackRange == 0) {
                "Zasięg ataku (attackRange) musi wynosić 0 dla jednostek nieposiadających zdolności ataku: $attackRange."
            }
        }
        require(defense >= 0) {
            "Obrona (defense) musi być nieujemna: $defense."
        }
        require(health > 0) {
            "Ilość punktów życia (health) musi być dodatnia: $health."
        }
        require(movement >= 0) {
            "Ilość punktów ruchu (movement) musi być nieujemna: $movement."
        }
        require(vision >= 0) {
            "Zasięg widzenia (vision) musi być nieujemny: $vision."
        }
        require(!cost.isNegative) {
            "Koszt zakupu jednostki (cost) musi być nieujemny: $cost."
        }
        require(!upkeep.isNegative) {
            "Koszt utrzymania jednostki (upkeep) musi być nieujemny: $upkeep."
        }
        require(limitPerCivilization == null || limitPerCivilization > 0) {
            "Limit ilości jednostek (limitPerCivilization) musi być dodatni lub pusty: $limitPerCivilization."
        }
    }

    val isMelee: Boolean
        get() = attackRange == 1

    val isRanged: Boolean
        get() = attackRange > 1
}
