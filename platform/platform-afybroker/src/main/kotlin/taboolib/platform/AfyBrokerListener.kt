package taboolib.platform


import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.plugin.EventBus
import net.afyer.afybroker.server.plugin.Listener
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.Inject
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.info
import taboolib.common.platform.service.PlatformListener
import taboolib.common.util.unsafeLazy
import java.lang.reflect.Method


@Awake
@Inject
@PlatformSide(Platform.AFYBROKER)
class AfyBrokerListener : PlatformListener {

    val plugin by unsafeLazy { AfyBrokerPlugin.getInstance() }

    val eventBus by unsafeLazy {
        Broker.getPluginManager().getProperty<EventBus>("eventBus")!!
    }

    val byListenerAndPriority by unsafeLazy {
        eventBus.getProperty<MutableMap<Class<*>, MutableMap<Byte, MutableMap<Any, Array<Method>>>>>("byListenerAndPriority")!!
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("Unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, level: Int, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = AfyBrokerListener(event, level) { func(it as T) }
        val prioritiesMap = byListenerAndPriority.computeIfAbsent(event) { HashMap() }
        val currentPriorityMap = prioritiesMap.computeIfAbsent(level.toByte()) { HashMap() }
        currentPriorityMap[listener] = arrayOf(AfyBrokerListener.handleMethod)
        eventBus.invokeMethod<Void>("bakeHandlers", event)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        val listener = proxyListener as AfyBrokerListener
        val priority = byListenerAndPriority[listener.cls] ?: return
        val listenerMap = priority[listener.level.toByte()] ?: return
        listenerMap.remove(listener)
        eventBus.invokeMethod<Void>("bakeHandlers", listener.cls)
    }

    class AfyBrokerListener(val cls: Class<*>, val level: Int, val consumer: (Any) -> Unit) : Listener, ProxyListener {

        fun handle(event: Any) {
            if (cls.isAssignableFrom(event.javaClass)) {
                consumer(event)
            }
        }

        companion object {

            val handleMethod: Method = AfyBrokerListener::class.java.getDeclaredMethod("handle", Any::class.java).also {
                it.isAccessible = true
            }
        }
    }
}