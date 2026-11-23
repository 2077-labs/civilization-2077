package com.labs2077.civilization.core.api

import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.PlayerId
import com.labs2077.civilization.core.api.Event as GameEvent
import com.labs2077.civilization.core.models.Error as GameError

interface Engine {
    fun dispatch(playerId: PlayerId, command: Command): Result<Collection<GameEvent>, GameError>
}
