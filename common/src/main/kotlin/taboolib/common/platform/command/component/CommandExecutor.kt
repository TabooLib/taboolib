package taboolib.common.platform.command.component

import taboolib.common.platform.command.CommandContext

class CommandExecutor<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) : CommandBinder<T>(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(commandBase: CommandBase, context: CommandContext<*>, argument: String) {
        val sender = cast(context)
        if (sender != null) {
            function.invoke(sender, (context as CommandContext<T>).copy(sender = sender), argument)
        } else {
            commandBase.commandIncorrectSender.exec(context, 0, 0)
        }
    }
}