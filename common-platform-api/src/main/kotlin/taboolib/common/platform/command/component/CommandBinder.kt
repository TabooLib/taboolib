package taboolib.common.platform.command.component

import taboolib.common.platform.command.CommandContext

abstract class CommandBinder<T>(val bind: Class<T>) {

    @Suppress("UNCHECKED_CAST")
    fun cast(context: CommandContext<*>): T? {
        val sender = context.sender()
        return when {
            bind.isInstance(sender) -> sender as? T
            bind.isInstance(sender.origin) -> sender.origin as T
            else -> null
        }
    }
}