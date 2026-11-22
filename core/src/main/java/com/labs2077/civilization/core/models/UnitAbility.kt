package com.labs2077.civilization.core.models

enum class UnitAbility {
    /** Jednostka może przeprowadzać ataki na inne jednostki.
     *  Rodzaj ataku i jego zasięg definiowany jest przez [UnitDefinition.attackRange]. */
    ATTACK,

    /** Jednostka może zakładać miasta. */
    FOUND_CITY,
}
