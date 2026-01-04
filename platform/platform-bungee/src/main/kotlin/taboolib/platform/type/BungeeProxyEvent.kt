package taboolib.platform.type

import net.md_5.bungee.api.plugin.Cancellable
import net.md_5.bungee.api.plugin.Event
import taboolib.common.PrimitiveIO.t
import taboolib.platform.BungeePlugin
import java.util.function.Consumer

open class BungeeProxyEvent : Event(), Cancellable {

    private var isCancelled = false
    private val cancelCallbacks = mutableListOf<Consumer<Array<StackTraceElement>>>()

    open val allowCancelled: Boolean
        get() = true

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
    fun onCancel(callback: Consumer<Array<StackTraceElement>>): BungeeProxyEvent {
        cancelCallbacks += callback
        return this
    }

    fun call(): Boolean {
        BungeePlugin.getInstance().proxy.pluginManager.callEvent(this)
        return !isCancelled
    }
}