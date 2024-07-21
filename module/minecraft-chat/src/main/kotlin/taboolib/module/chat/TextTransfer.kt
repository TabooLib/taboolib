package taboolib.module.chat

import taboolib.module.chat.impl.DefaultSimpleComponent

/**
 * TabooLib
 * taboolib.module.chat.TextTransfer
 *
 * @author 坏黑
 * @since 2023/2/9 21:49
 */
class TextTransfer(val component: DefaultSimpleComponent) {

    internal val transforms = arrayListOf<(String) -> String>()

    /** 转换文本 */
    internal operator fun invoke(text: Any?): String {
        var t = text.toString()
        transforms.forEach { t = it(t) }
        return t
    }

    /** 添加转换器 */
    fun transform(block: (String) -> String): TextTransfer {
        transforms += block
        return this
    }

    /** 上色 */
    fun colored(): TextTransfer {
        return transform { it.colored() }
    }

    /** 去色 */
    fun uncolored(): TextTransfer {
        return transform { it.uncolored() }
    }
}