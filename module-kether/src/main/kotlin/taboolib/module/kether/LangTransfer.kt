package taboolib.module.kether

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.TextTransfer

/**
 * TabooLib
 * taboolib.module.lang.TextTransferKether
 *
 * @author sky
 * @since 2021/6/20 11:08 下午
 */
object LangTransfer : TextTransfer {

    val cacheMap = KetherFunction.Cache()

    override fun translate(sender: ProxyCommandSender, source: String): String {
        if (source.contains("{{")) {
            return KetherFunction.parse(source, cache = cacheMap, sender = sender)
        }
        return source
    }
}