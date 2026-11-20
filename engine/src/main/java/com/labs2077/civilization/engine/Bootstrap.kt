package com.labs2077.civilization.engine

import com.labs2077.civilization.core.api.commands.GreetingCommand
import com.labs2077.civilization.engine.dispatch.CommandHandlerRegistry
import com.labs2077.civilization.engine.dispatch.DefaultCommandHandlerRegistry
import com.labs2077.civilization.engine.handlers.GreetingCommandHandler
import com.labs2077.civilization.core.api.Engine as GameEngine

fun createEngine(): GameEngine = Engine(
    registry = createCommandHandlerRegistry()
)

private fun createCommandHandlerRegistry(): CommandHandlerRegistry =
    DefaultCommandHandlerRegistry().apply {
        add<GreetingCommand>(GreetingCommandHandler())
    }
