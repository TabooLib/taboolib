package taboolib.platform

import de.dytanic.cloudnet.driver.event.EventListener
import taboolib.common.platform.event.ProxyListener
import taboolib.internal.ActiveListener
import taboolib.internal.Internal

/**
 * @author Leosouthey
 * @since 2022/2/6-16:31
 **/
@Internal
class CloudNetV3RegisteredListener(clazz: Class<*>, consumer: (Any) -> Unit) : ActiveListener(clazz, consumer), ProxyListener {

    @EventListener
    fun handle(event: Any) {
        process(event)
    }
}