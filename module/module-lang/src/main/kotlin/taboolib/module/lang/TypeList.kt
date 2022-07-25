package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.module.lang.TypeList
 *
 * @author sky
 * @since 2021/7/30 12:38 下午
 */
class TypeList(val list: List<Type>) : Type {

    fun asTextList(sender: ProxyCommandSender, vararg args: Any): List<String> {
        return list.filterIsInstance<TypeText>().mapNotNull { it.asText(sender, *args) }
    }

    fun asTextList(sender: ProxyCommandSender, func: (String) -> (String)): List<String> {
        return list.filterIsInstance<TypeText>().mapNotNull { it.asText(sender, func) }
    }

    override fun init(source: Map<String, Any>) {
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        list.forEach { it.send(sender, *args) }
    }

    override fun send(sender: ProxyCommandSender, func: (String?) -> String?) {
        list.forEach { it.send(sender, func) }
    }

    override fun toString(): String {
        return "TypeList(list=$list)"
    }
}