package com.labs2077.civilization.engine.dispatch

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.api.Event as GameEvent
import com.labs2077.civilization.core.models.Error as GameError

internal interface CommandHandler<C : Command> {
    fun handle(command: C): Result<Collection<GameEvent>, GameError>
}
