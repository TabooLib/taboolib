package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender

@Suppress("UNCHECKED_CAST")
abstract class Action<T>(val bind: Class<T>) {

    protected fun CommandContext<*>.getSender(): T? {
        val sender = sender as ProxyCommandSender
        return when {
            bind.isInstance(sender) -> sender as? T
            bind.isInstance(sender.origin) -> sender.origin as T
            else -> null
        }
    }
}