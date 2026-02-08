package taboolib.platform.type

import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.metadata.MetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.plugin.Plugin
import taboolib.common.PrimitiveIO.t
import java.util.concurrent.ConcurrentHashMap

@Suppress("SameReturnValue")
open class BukkitProxyEvent : Event(!Bukkit.isPrimaryThread()), Cancellable, Metadatable {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(value: Boolean) {
        if (allowCancelled) {
            isCancelled = value
        } else {
            error(t("这个事件无法被取消。", "This event cannot be cancelled."))
        }
    }

    fun call(): Boolean {
        Bukkit.getPluginManager().callEvent(this)
        return !isCancelled
    }

    val metadataMap = ConcurrentHashMap<String, MetadataValue>()

    override fun setMetadata(key: String, value: MetadataValue) {
        metadataMap[key] = value
    }

    override fun getMetadata(key: String): MutableList<MetadataValue> {
        return metadataMap[key]?.let { mutableListOf(it) } ?: mutableListOf()
    }

    override fun hasMetadata(key: String): Boolean {
        return metadataMap.containsKey(key)
    }

    override fun removeMetadata(key: String, plugin: Plugin) {
        metadataMap.remove(key)
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