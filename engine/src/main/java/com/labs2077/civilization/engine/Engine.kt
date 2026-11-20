package com.labs2077.civilization.engine

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.engine.dispatch.CommandHandler
import com.labs2077.civilization.engine.dispatch.CommandHandlerRegistry
import com.labs2077.civilization.core.api.Engine as GameEngine
import com.labs2077.civilization.core.api.Event as GameEvent

internal class Engine(
    private val registry: CommandHandlerRegistry,
) : GameEngine {

    override fun dispatch(command: Command): Result<Collection<GameEvent>, Error> {
        return requireCommandHandler(command).handle(command)
    }

    private fun <C : Command> requireCommandHandler(command: C): CommandHandler<C> =
        requireNotNull(registry[command]) {
            "Nie odnaleziono procedury obsługującej polecenie: ${command::class.simpleName}."
        }
}
