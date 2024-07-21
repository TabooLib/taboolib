package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.component

/**
 * TabooLib
 * taboolib.module.lang.TypeText
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeText : Type {

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
            val newText = text!!.translate(sender, *args).replaceWithOrder(*args)
            if (Language.enableSimpleComponent) {
                newText.component().sendTo(sender)
            } else {
                sender.sendMessage(newText)
            }
        }
    }

    override fun toString(): String {
        return "NodeText(text=$text)"
    }
}