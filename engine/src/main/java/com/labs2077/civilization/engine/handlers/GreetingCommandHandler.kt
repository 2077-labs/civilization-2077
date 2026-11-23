package com.labs2077.civilization.engine.handlers

import com.labs2077.civilization.core.api.commands.GreetingCommand
import com.labs2077.civilization.core.api.events.GreetingEvent
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.engine.Context
import com.labs2077.civilization.engine.State
import com.labs2077.civilization.engine.dispatch.BaseCommandHandler

internal class GreetingCommandHandler(context: Context, state: State) :
    BaseCommandHandler<GreetingCommand>(context, state) {

    override fun execute(command: GreetingCommand): Error? {
        val greeting = "Nice to meet you, ${command.recipient}!"

        emit(GreetingEvent(greeting))

        return null
    }
}
