package com.labs2077.civilization.engine

import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.Player

internal data class Context(
    val player: Player,
    val civilization: Civilization,
)
