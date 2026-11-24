package com.labs2077.civilization.engine

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.api.commands.GreetingCommand
import com.labs2077.civilization.core.api.commands.MoveUnitCommand
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.GameState
import com.labs2077.civilization.core.models.PlayerId
import com.labs2077.civilization.engine.dispatch.CommandHandlerFactoryRegistry
import com.labs2077.civilization.engine.handlers.GreetingCommandHandler
import com.labs2077.civilization.engine.handlers.MoveUnitCommandHandler
import com.labs2077.civilization.core.api.Engine as GameEngine
import com.labs2077.civilization.engine.State as GameState

internal class Engine(
    private val state: GameState,
    private val registry: CommandHandlerFactoryRegistry,
) : GameEngine {

    override fun dispatch(playerId: PlayerId, command: Command): Result<List<Event>, Error> {
        val factory = requireNotNull(registry.get(command::class)) {
            "Nie odnaleziono procedury obsługującej polecenie: ${command::class.simpleName}."
        }

        val context = createContext(playerId)
        val handler = factory.create(context, state)

        return handler.handle(command)
    }

    private fun createContext(playerId: PlayerId): Context = with(state) {
        val player = players.firstOrNull {
            it.id == playerId
        }
        val civilization = civilizations.firstOrNull {
            it.player.id == playerId
        }

        requireNotNull(player) {
            "Nie odnaleziono gracza: ${playerId.value}."
        }
        requireNotNull(civilization) {
            "Nie odnaleziono cywilizacji gracza: ${playerId.value}."
        }

        Context(player, civilization)
    }
}

fun createEngine(state: GameState): GameEngine {
    val registry = CommandHandlerFactoryRegistry().apply {
        add(GreetingCommand::class, ::GreetingCommandHandler)
        add(MoveUnitCommand::class, ::MoveUnitCommandHandler)
    }
    return Engine(state, registry)
}
