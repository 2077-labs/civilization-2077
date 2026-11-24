package com.labs2077.civilization.core.api

import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.GameState
import com.labs2077.civilization.core.models.PlayerId
import kotlinx.coroutines.flow.SharedFlow
import com.labs2077.civilization.core.api.Event as GameEvent
import com.labs2077.civilization.core.models.Error as GameError

interface Engine {

    val state: GameState // TODO: To się jeszcze zmieni
    val events: SharedFlow<GameEvent> // TODO: To się jeszcze zmieni

    fun dispatch(playerId: PlayerId, command: Command): Result<Collection<GameEvent>, GameError>
}
