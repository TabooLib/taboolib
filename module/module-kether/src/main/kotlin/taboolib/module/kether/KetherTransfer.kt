package taboolib.module.kether

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.TextTransfer

/**
 * TabooLib
 * taboolib.module.lang.TransferKether
 *
 * @author sky
 * @since 2021/6/20 11:08 下午
 */
object KetherTransfer : TextTransfer {

    val cacheMap = KetherShell.Cache()
    val namespace = ArrayList<String>()

    override fun translate(sender: ProxyCommandSender, source: String): String {
        if (source.contains("{{")) {
            return KetherFunction.parse(source, cache = cacheMap, sender = sender, namespace = namespace)
        }
        return source
    }
}