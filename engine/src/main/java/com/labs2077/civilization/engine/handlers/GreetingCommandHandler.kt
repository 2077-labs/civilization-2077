package com.labs2077.civilization.engine.handlers

import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.api.commands.GreetingCommand
import com.labs2077.civilization.core.api.events.GreetingEvent
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.engine.dispatch.CommandHandler

internal class GreetingCommandHandler : CommandHandler<GreetingCommand> {

    override fun handle(command: GreetingCommand): Result<Collection<Event>, Error> {
        val greeting = "Nice to meet you, ${command.recipient}!"
        val event = GreetingEvent(greeting)

        return Result.Success(listOf(event))
    }
}
