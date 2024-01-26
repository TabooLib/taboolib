package taboolib.common.platform.command.component

import taboolib.common.platform.command.CommandContext

class CommandUnknownNotify<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, index: Int, state: Int) -> Unit) : CommandBinder<T>(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext<*>, index: Int, state: Int) {
        val sender = cast(context)
        if (sender != null) {
            function.invoke(sender, (context as CommandContext<T>).copy(sender = sender), index, state)
        }
    }
}