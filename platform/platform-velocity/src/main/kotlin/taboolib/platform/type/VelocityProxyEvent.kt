package taboolib.platform.type

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import taboolib.common.PrimitiveIO.t
import taboolib.platform.VelocityPlugin
import java.util.function.Consumer

open class VelocityProxyEvent : ResultedEvent<GenericResult> {

    private var isCancelled = false
    private val cancelCallbacks = mutableListOf<Consumer<Array<StackTraceElement>>>()

    open val allowCancelled: Boolean
        get() = true

    override fun getResult(): GenericResult {
        return if (isCancelled) GenericResult.denied() else GenericResult.allowed()
    }

    override fun setResult(result: GenericResult) {
        if (allowCancelled) {
            isCancelled = !result.isAllowed
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
    fun onCancel(callback: Consumer<Array<StackTraceElement>>): VelocityProxyEvent {
        cancelCallbacks += callback
        return this
    }

    fun call(): Boolean {
        VelocityPlugin.getInstance().server.eventManager.fire(this)
        return !isCancelled
    }
}