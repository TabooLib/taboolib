package taboolib.common.platform.command

class ActionRestrict<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean) : Action<T>(bind) {

    fun exec(context: CommandContext<*>, argument: String): Boolean? {
        val sender = context.getSender()
        return if (sender != null) {
            function.invoke(sender, CommandContext(sender, context.command, context.name, context.compound, context.args, context.index), argument)
        } else {
            null
        }
    }
}