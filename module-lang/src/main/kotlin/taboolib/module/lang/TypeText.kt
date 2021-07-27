package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.asList
import taboolib.common.util.replaceWithOrder

/**
 * TabooLib
 * taboolib.module.lang.TypeText
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeText : Type {

    var text: List<String>? = null

    fun asText(sender: ProxyCommandSender, def: String? = null, vararg args: Any): String? {
        return text?.getOrNull(0)?.replaceWithOrder(*args)?.translate(sender) ?: def
    }

    fun asTextList(sender: ProxyCommandSender, vararg args: Any): List<String> {
        return (text ?: emptyList()).map { it.replaceWithOrder(*args).translate(sender) }
    }

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.asList()
        // if blocked
        if (text?.all { it.isEmpty() } == true) {
            text = null
        }
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        text?.forEach { sender.sendMessage(it.replaceWithOrder(*args).translate(sender)) }
    }

    override fun toString(): String {
        return "NodeText(text=$text)"
    }
}