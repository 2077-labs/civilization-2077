package com.labs2077.civilization.core.models

import kotlin.uuid.Uuid

@JvmInline
value class PlayerId(
    val value: Uuid,
)

class Player(
    val id: PlayerId = PlayerId(value = Uuid.random()),
)
