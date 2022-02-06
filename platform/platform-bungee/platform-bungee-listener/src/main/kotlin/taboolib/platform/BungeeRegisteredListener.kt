package taboolib.platform

import taboolib.common.platform.event.ProxyListener
import taboolib.internal.ActiveListener
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Internal
class BungeeRegisteredListener(val level: Int, clazz: Class<*>, consumer: (Any) -> Unit) : ActiveListener(clazz, consumer), ProxyListener {

    fun handle(event: Any) {
        process(event)
    }
}