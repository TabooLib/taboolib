package taboolib.platform

import cn.nukkit.event.Event
import cn.nukkit.event.Listener
import cn.nukkit.plugin.EventExecutor
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.isPlatformEvent
import taboolib.internal.ActiveListener
import taboolib.internal.Internal
import java.util.concurrent.CopyOnWriteArraySet

/**
 * @author Leosouthey
 * @since 2022/2/6-16:25
 **/
@Internal
class NukkitRegisteredListener(clazz: Class<*>, consumer: (Any) -> Unit) : ActiveListener(clazz, consumer), Listener, EventExecutor, ProxyListener {

    val isVanillaEvent = Event::class.java.isAssignableFrom(clazz)
    val ignored = CopyOnWriteArraySet<Class<*>>()

    override fun execute(listener: Listener, event: Event) {
        process(event)
    }
}