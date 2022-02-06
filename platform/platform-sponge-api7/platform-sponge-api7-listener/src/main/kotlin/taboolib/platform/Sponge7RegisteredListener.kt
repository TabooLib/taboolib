package taboolib.platform

import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventListener
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.isPlatformEvent
import taboolib.internal.ActiveListener

/**
 * @author Leosouthey
 * @since 2022/2/6-17:54
 **/
class Sponge7RegisteredListener<T : Event>(clazz: Class<*>, consumer: (Any) -> Unit) : ActiveListener(clazz, consumer), EventListener<T>, ProxyListener {

    override fun handle(event: T) {
        process(event)
    }
}