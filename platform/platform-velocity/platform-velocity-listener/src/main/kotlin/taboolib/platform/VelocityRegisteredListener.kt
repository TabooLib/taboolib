package taboolib.platform

import com.velocitypowered.api.event.EventHandler
import taboolib.common.platform.event.ProxyListener
import taboolib.internal.ActiveListener
import taboolib.internal.Internal

/**
 * @author Leosouthey
 * @since 2022/2/6-16:40
 **/
@Internal
class VelocityRegisteredListener(clazz: Class<*>, consumer: (Any) -> Unit) : ActiveListener(clazz, consumer), ProxyListener, EventHandler<Any> {

    override fun execute(event: Any) {
        process(event)
    }
}