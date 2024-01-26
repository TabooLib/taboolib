package taboolib.common.platform.command.component

import taboolib.common.platform.command.CommandContext

class CommandRestrict<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean) : CommandBinder<T>(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext<*>, argument: String): Boolean? {
        val sender = cast(context)
        return if (sender != null) {
            function.invoke(sender, (context as CommandContext<T>).copy(sender = sender), argument)
        } else {
            null
        }
    }
}