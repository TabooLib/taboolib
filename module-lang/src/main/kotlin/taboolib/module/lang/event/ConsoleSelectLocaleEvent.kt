package taboolib.module.lang.event

import taboolib.common.platform.ProxyEvent
import taboolib.common.platform.ProxyPlayer

/**
 * TabooLib
 * taboolib.module.lang.event.ConsoleSelectLocaleEvent
 *
 * @author sky
 * @since 2021/6/18 11:05 下午
 */
class ConsoleSelectLocaleEvent(var locale: String) : ProxyEvent() {

    override val allowCancelled: Boolean
        get() = false
}