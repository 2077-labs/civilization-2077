package com.labs2077.civilization.engine.dispatch

import com.labs2077.civilization.core.api.Command
import kotlin.reflect.KClass

internal class CommandHandlerFactoryRegistry {

    private val factories = mutableMapOf<KClass<*>, CommandHandlerFactory<*>>()

    fun <C : Command> add(clazz: KClass<C>, factory: CommandHandlerFactory<C>) {
        factories[clazz] = factory
    }

    fun <C : Command> get(clazz: KClass<C>): CommandHandlerFactory<Command>? {
        @Suppress("UNCHECKED_CAST") return factories[clazz] as? CommandHandlerFactory<Command>
    }
}
