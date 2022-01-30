package taboolib.common.platform.command

import taboolib.common.boot.SimpleServiceLoader
import taboolib.common.platform.ProxyCommandSender

abstract class Component : Section(false) {

    abstract fun createCompound(): Component

    abstract fun execute(context: CommandContext<*>): Boolean

    abstract fun suggest(context: CommandContext<*>): List<String>?

    abstract fun incorrectSender(function: (sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>) -> Unit)

    abstract fun incorrectCommand(function: (sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, index: Int, state: Int) -> Unit)

    abstract fun sendIncorrectSender(context: CommandContext<*>)

    abstract fun sendIncorrectCommand(context: CommandContext<*>, index: Int, state: Int)

    abstract fun setResult(value: Boolean)

    companion object {

        @JvmField
        val INSTANCE: Component = SimpleServiceLoader.load(Component::class.java)
    }
}