package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.kether.Kether
import taboolib.module.kether.KetherFunction

/**
 * TabooLib
 * taboolib.module.lang.TextTransferKether
 *
 * @author sky
 * @since 2021/6/20 11:08 下午
 */
object TextTransferKether : TextTransfer {

    var hooked = false
    val cacheMap = KetherFunction.Cache()

    init {
        try {
            Kether.registry
            hooked = true
        } catch (ex: Throwable) {
        }
    }

    override fun translate(sender: ProxyCommandSender, source: String): String {
        if (hooked && source.contains("{{")) {
            return KetherFunction.parse(source, cache = cacheMap) {
                this.sender = sender
            }
        }
        return source
    }
}