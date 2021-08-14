package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.util.asList

/**
 * TabooLib
 * taboolib.module.lang.TypeCommand
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeCommand : Type {

    var command: List<String>? = null

    override fun init(source: Map<String, Any>) {
        command = source["command"]?.asList()
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        command?.forEach { console().performCommand(it.replace("@p", sender.name)) }
    }

    override fun toString(): String {
        return "TypeCommand(command='$command')"
    }
}