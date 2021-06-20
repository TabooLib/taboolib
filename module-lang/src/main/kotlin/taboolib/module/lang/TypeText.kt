package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.asList
import taboolib.common.util.replaceWithOrder
import taboolib.module.lang.Language.translate

/**
 * TabooLib
 * taboolib.module.lang.TypeText
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeText : Type {

    var text: List<String>? = null

    fun asText(def: String? = null) = text?.getOrNull(0) ?: def

    fun asTextList() = text ?: emptyList()

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.asList()
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        text?.forEach { sender.sendMessage(it.replaceWithOrder(*args).translate(sender)) }
    }

    override fun toString(): String {
        return "NodeText(text=$text)"
    }
}