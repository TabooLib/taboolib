package taboolib.common.platform.command.component

import taboolib.common.platform.command.CommandContext

class CommandSuggestion<T>(bind: Class<T>, val uncheck: Boolean, val function: (sender: T, context: CommandContext<T>) -> List<String>?) : CommandBinder<T>(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext<*>): List<String>? {
        val sender = cast(context)
        return if (sender != null) {
            function.invoke(sender, (context as CommandContext<T>).copy(sender = sender))
        } else {
            null
        }
    }
}