package taboolib.module.lang.event

import taboolib.common.platform.event.ProxyEvent

/**
 * TabooLib
 * taboolib.module.lang.event.SystemSelectLocaleEvent
 *
 * @author sky
 * @since 2021/6/18 11:05 下午
 */
class SystemSelectLocaleEvent(var locale: String) : ProxyEvent() {

    override val allowCancelled: Boolean
        get() = false
}