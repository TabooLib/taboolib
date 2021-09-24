package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.module.lang.TextTransfer
 *
 * @author sky
 * @since 2021/6/20 11:07 下午
 */
interface TextTransfer {

    fun translate(sender: ProxyCommandSender, source: String): String
}