package taboolib.common.platform.command

class ActionIncorrect<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, index: Int, state: Int) -> Unit) : Action<T>(bind) {

    fun exec(context: CommandContext<*>, index: Int, state: Int) {
        val sender = context.getSender()

        sender?.let {
            val newContext: CommandContext<T> =
                CommandContext(it, context.command, context.name, context.compound, context.args, context.index)

            function(sender, newContext, index, state)
        }
    }
}