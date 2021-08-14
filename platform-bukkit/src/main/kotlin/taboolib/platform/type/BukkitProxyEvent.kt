package taboolib.platform.type

import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.reflect.Reflex.Companion.setProperty

open class BukkitProxyEvent(val proxyEvent: ProxyEvent? = null) : Event(), Cancellable {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    init {
        if (proxyEvent == null || proxyEvent.allowAsynchronous) {
            setProperty("async", !Bukkit.isPrimaryThread())
        }
    }

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }

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
            error("unsupported")
        }
    }

    fun call(): Boolean {
        Bukkit.getPluginManager().callEvent(this)
        return !isCancelled
    }

    companion object {

        @JvmField
        val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }
}