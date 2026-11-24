package com.labs2077.civilization.engine.dispatch

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.GameState
import com.labs2077.civilization.engine.Context

internal abstract class BaseCommandHandler<C : Command>(
    val context: Context, val state: GameState,
) : CommandHandler<C> {

    private val events = mutableListOf<Event>()

    protected fun emit(vararg events: Event) {
        this.events.addAll(events)
    }

    protected fun emitAll(events: Collection<Event>) {
        this.events.addAll(events)
    }

    override fun handle(command: C): Result<List<Event>, Error> {
        events.clear()

        return when (val error = execute(command)) {
            is Any -> Result.Error(error)
            else -> Result.Success(events)
        }
    }

    protected abstract fun execute(command: C): Error?
}
