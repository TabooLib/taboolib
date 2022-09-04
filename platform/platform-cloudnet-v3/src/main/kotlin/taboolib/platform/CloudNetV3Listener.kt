package taboolib.platform

import de.dytanic.cloudnet.CloudNet
import de.dytanic.cloudnet.driver.event.EventListener
import de.dytanic.cloudnet.driver.event.events.DriverEvent
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventOrder
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.getUsableEvent
import taboolib.common.platform.function.isPlatformEvent
import taboolib.common.platform.service.PlatformListener
import org.tabooproject.reflex.Reflex.Companion.getProperty

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.CLOUDNET_V3])
class CloudNetV3Listener : PlatformListener {

    val plugin by unsafeLazy { CloudNetV3Plugin.getInstance() }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, order: EventOrder, beforeModifications: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = CloudNetV3Listener(event) { func(it as T) }
        CloudNet.getInstance().eventManager.registerListener(listener)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        CloudNet.getInstance().eventManager.unregisterListener(proxyListener)
    }

    class CloudNetV3Listener(val clazz: Class<*>, val consumer: (Any) -> Unit) : ProxyListener {

        @EventListener
        fun handle(event: Any) {
            val origin = if (event::class.java.isPlatformEvent) event.getProperty<Any>("proxyEvent") ?: event else event
            if (clazz.isAssignableFrom(origin.javaClass)) {
                consumer(origin)
            }
        }
    }
}