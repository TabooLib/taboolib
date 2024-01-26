package taboolib.platform.type

import net.md_5.bungee.api.plugin.Cancellable
import net.md_5.bungee.api.plugin.Event
import taboolib.common.platform.event.ProxyEvent
import taboolib.platform.BungeePlugin

open class BungeeProxyEvent(val proxyEvent: ProxyEvent? = null) : Event(), Cancellable {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    override fun isCancelled(): Boolean {
        return proxyEvent?.isCancelled ?: isCancelled
    }

    override fun setCancelled(value: Boolean) {
        if (proxyEvent != null) {
            if (proxyEvent.allowCancelled) {
                proxyEvent.isCancelled = value
            } else {
                error("Unsupported")
            }
        } else if (allowCancelled) {
            isCancelled = value
        } else {
            error("Event cannot be cancelled.")
        }
    }

    fun call(): Boolean {
        BungeePlugin.getInstance().proxy.pluginManager.callEvent(this)
        return !isCancelled
    }
}