package com.labs2077.civilization.engine.dispatch

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.models.GameState
import com.labs2077.civilization.engine.Context

internal fun interface CommandHandlerFactory<C : Command> {
    fun create(context: Context, state: GameState): CommandHandler<C>
}
