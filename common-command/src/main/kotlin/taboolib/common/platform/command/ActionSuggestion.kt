package taboolib.common.platform.command

class ActionSuggestion<T>(bind: Class<T>, val uncheck: Boolean, val function: (sender: T, context: CommandContext<T>) -> List<String>?) : Action<T>(bind) {

    fun exec(context: CommandContext<*>): List<String>? {
        val sender = context.getSender()
        return if (sender != null) {
            function.invoke(sender, CommandContext(sender, context.command, context.name, context.compound, context.args, context.index))
        } else {
            null
        }
    }
}