package taboolib.platform

import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import taboolib.common.platform.event.ProxyListener
import taboolib.internal.ActiveListener
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.BukkitRegisteredListener
 *
 * @author sky
 * @since 2021/6/17 12:22 上午
 */
@Internal
class BukkitRegisteredListener(clazz: Class<*>, consumer: (Any) -> Unit) : ActiveListener(clazz, consumer), Listener, EventExecutor, ProxyListener {

    override fun execute(listener: Listener, event: Event) {
        process(event)
    }
}