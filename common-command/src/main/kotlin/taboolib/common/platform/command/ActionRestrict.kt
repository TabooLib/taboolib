package taboolib.common.platform.command

class ActionRestrict<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean) : Action<T>(bind) {

    fun exec(context: CommandContext<*>, argument: String): Boolean? {
        val sender = context.getSender()
        return sender?.let {
            val newContext = CommandContext<T>(sender, context.command, context.name, context.compound, context.args, context.index)
            function(it, newContext, argument)
        }
    }
}