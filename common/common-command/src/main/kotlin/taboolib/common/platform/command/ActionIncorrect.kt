package taboolib.common.platform.command

class ActionIncorrect<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, index: Int, state: Int) -> Unit) : Action<T>(bind) {

    fun exec(context: CommandContext<*>, index: Int, state: Int) {
        val sender = context.getSender()
        if (sender != null) {
            function.invoke(sender, CommandContext(sender, context.command, context.name, context.compound, context.args, context.index), index, state)
        }
    }
}