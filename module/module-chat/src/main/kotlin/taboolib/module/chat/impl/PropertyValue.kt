package taboolib.module.chat.impl

import taboolib.module.chat.ComponentText
import taboolib.module.chat.TextTransfer

/**
 * TabooLib
 * taboolib.module.chat.impl.PropertyValue
 *
 * @author 坏黑
 * @since 2023/2/9 21:35
 */
interface PropertyValue {

    /** 文本 */
    class Text(val text: String) : PropertyValue {

        override fun toString(): String {
            return text
        }
    }

    /** 链接 */
    class Link(val name: String) : PropertyValue {

        /** 获取链接对应的值 */
        fun getValue(transfer: TextTransfer): ComponentText {
            return transfer.component.linkData[name]!!.build(transfer)
        }

        override fun toString(): String {
            return name
        }
    }
}