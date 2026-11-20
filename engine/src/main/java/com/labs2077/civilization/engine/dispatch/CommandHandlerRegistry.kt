package com.labs2077.civilization.engine.dispatch

import com.labs2077.civilization.core.api.Command
import kotlin.reflect.KClass

internal interface CommandHandlerRegistry {
    operator fun <C : Command> get(command: C): CommandHandler<C>?
    operator fun <C : Command> get(clazz: KClass<C>): CommandHandler<C>?
}
