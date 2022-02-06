package taboolib.platform

import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventListener
import taboolib.common.platform.event.ProxyListener
import taboolib.internal.ActiveListener
import taboolib.internal.Internal

/**
 * @author Leosouthey
 * @since 2022/2/6-18:42
 **/
@Internal
class Sponge8RegisteredListener<T : Event>(clazz: Class<*>, consumer: (Any) -> Unit) : ActiveListener(clazz, consumer), EventListener<T>, ProxyListener {

    override fun handle(event: T) {
        process(event)
    }
}