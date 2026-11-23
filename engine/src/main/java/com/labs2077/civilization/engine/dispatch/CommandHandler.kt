package com.labs2077.civilization.engine.dispatch

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.Error

internal fun interface CommandHandler<C : Command> {
    fun handle(command: C): Result<List<Event>, Error>
}
