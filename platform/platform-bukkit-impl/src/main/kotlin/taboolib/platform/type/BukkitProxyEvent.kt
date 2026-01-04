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
import java.util.function.Consumer

@Suppress("SameReturnValue")
open class BukkitProxyEvent : Event(!Bukkit.isPrimaryThread()), Cancellable, Metadatable {

    private var isCancelled = false
    private val cancelCallbacks = mutableListOf<Consumer<Array<StackTraceElement>>>()

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
            if (cancelCallbacks.isNotEmpty()) {
                val stackTrace = Thread.currentThread().stackTrace
                cancelCallbacks.forEach { it.accept(stackTrace) }
            }
        } else {
            error(t("这个事件无法被取消。", "This event cannot be cancelled."))
        }
    }

    /**
     * 注册取消回调，当事件取消状态被设置时触发
     * @param callback 回调函数，参数为调用堆栈
     */
    fun onCancel(callback: Consumer<Array<StackTraceElement>>): BukkitProxyEvent {
        cancelCallbacks += callback
        return this
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