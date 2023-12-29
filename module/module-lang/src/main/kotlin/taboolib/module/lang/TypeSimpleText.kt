package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.component

/**
 * TabooLib
 * taboolib.module.lang.TypeSimpleText
 *
 * test:
 * - ==: simple_text
 *   text: |-
 *     [你好](c=black)
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeSimpleText : Type {

    var text: String? = null

    constructor()
    constructor(text: String) {
        if (text.isNotEmpty()) {
            this.text = text
        }
    }

    fun asText(sender: ProxyCommandSender, vararg args: Any): String? {
        return text?.translate(sender, *args)?.replaceWithOrder(*args)
    }

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.toString()
        // if blank, set null
        if (text?.isEmpty() == true) {
            text = null
        }
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        if (text != null) {
            text!!.translate(sender, *args).replaceWithOrder(*args).component().sendTo(sender)
        }
    }

    override fun toString(): String {
        return "NodeText(text=$text)"
    }
}