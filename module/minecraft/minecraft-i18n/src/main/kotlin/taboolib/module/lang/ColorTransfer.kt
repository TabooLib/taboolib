package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.chat.HexColor
import taboolib.module.chat.colored

/**
 * 颜色转换
 */
object ColorTransfer : TextTransfer {

    /**
     * 是否启用颜色模块
     */
    val isSupported = try {
        HexColor.translate("")
        true
    } catch (_: NoClassDefFoundError) {
        false
    }

    override fun translate(sender: ProxyCommandSender, source: String, vararg args: Any): String {
        return source.colored()
    }
}