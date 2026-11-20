package com.labs2077.civilization.engine.dispatch

import com.labs2077.civilization.core.api.Command
import kotlin.reflect.KClass

internal class DefaultCommandHandlerRegistry : CommandHandlerRegistry {

    private val handlers = mutableMapOf<KClass<out Command>, CommandHandler<*>>()

    inline fun <reified C : Command> add(handler: CommandHandler<C>) {
        add(C::class, handler)
    }

    fun <C : Command> add(clazz: KClass<C>, handler: CommandHandler<C>) {
        handlers[clazz] = handler
    }

    override fun <C : Command> get(command: C): CommandHandler<C>? {
        return internalGetHandler(command::class)
    }

    override fun <C : Command> get(clazz: KClass<C>): CommandHandler<C>? {
        return internalGetHandler(clazz)
    }

    private fun <C : Command> internalGetHandler(clazz: KClass<out C>): CommandHandler<C>? {
        @Suppress("UNCHECKED_CAST") return handlers[clazz] as? CommandHandler<C>
    }
}
