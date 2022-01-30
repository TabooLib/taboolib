package taboolib.common.platform.command

class ActionExecute<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) : Action<T>(bind) {

    fun exec(component: Component, context: CommandContext<*>, argument: String) {
        val sender = context.getSender()
        if (sender != null) {
            function.invoke(sender, CommandContext(sender, context.command, context.name, context.compound, context.args, context.index), argument)
        } else {
            component.sendIncorrectSender(context)
        }
    }
}