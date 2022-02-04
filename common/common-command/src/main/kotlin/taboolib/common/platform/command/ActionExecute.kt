package taboolib.common.platform.command

import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.getProxyPlayer

class ActionExecute<T>(bind: Class<T>, val function: Helper.(sender: T, context: CommandContext<T>, argument: String) -> Unit) : Action<T>(bind) {

    fun exec(component: Component, context: CommandContext<*>, argument: String) {
        val sender = context.getSender()
        if (sender != null) {
            val clone = CommandContext<T>(sender, context.command, context.name, context.compound, context.args, context.index)
            function.invoke(Helper(clone), sender, clone, argument)
        } else {
            component.sendIncorrectSender(context)
        }
    }

    class Helper(private val context: CommandContext<*>) {

        fun player(offset: Int = 0): ProxyPlayer {
            return playerOrNull(offset)!!
        }

        fun playerOrNull(offset: Int = 0): ProxyPlayer? {
            return getProxyPlayer(context.argument(offset))
        }

        fun argument(offset: Int = 0): String {
            return context.argument(offset)
        }

        fun argumentOrNull(offset: Int = 0): String? {
            return context.argumentOrNull(offset)
        }
    }
}