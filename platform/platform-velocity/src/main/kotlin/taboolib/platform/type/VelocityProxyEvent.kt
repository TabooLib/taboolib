package taboolib.platform.type

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import org.slf4j.LoggerFactory
import taboolib.common.PrimitiveIO.t
import taboolib.platform.VelocityPlugin
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

open class VelocityProxyEvent : ResultedEvent<GenericResult> {

    @Volatile
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

    /**
     * 调用事件，并在所有监听器完成后返回事件是否未被取消。
     */
    fun callAsync(): CompletableFuture<Boolean> {
        return fireEvent().thenApply { !isCancelled }
    }

    /**
     * 调用事件但不等待异步监听器。
     *
     * 若事件已同步完成，则返回最终状态；否则返回调用时可见的取消状态快照。
     */
    fun call(): Boolean {
        val future = fireEvent()
        val snapshot = !isCancelled
        future.whenComplete { _, throwable ->
            if (throwable != null) {
                reportCallFailure(throwable)
            }
        }
        return if (future.isDone) !isCancelled else snapshot
    }

    /**
     * 为测试保留的事件派发接缝。
     */
    protected open fun fireEvent(): CompletableFuture<VelocityProxyEvent> {
        return VelocityPlugin.getInstance().server.eventManager.fire(this)
    }

    private fun reportCallFailure(throwable: Throwable) {
        try {
            onCallFailure(throwable)
        } catch (reportingFailure: Throwable) {
            throwable.addSuppressed(reportingFailure)
            try {
                LoggerFactory.getLogger(VelocityProxyEvent::class.java)
                    .error("Failed to report an asynchronous Velocity event failure", throwable)
            } catch (fallbackFailure: Throwable) {
                throwable.addSuppressed(fallbackFailure)
                try {
                    throwable.printStackTrace()
                } catch (_: Throwable) {
                }
            }
        }
    }

    /**
     * 兼容调用无法向调用方传播异步异常，因此至少将其记录下来。
     */
    protected open fun onCallFailure(throwable: Throwable) {
        VelocityPlugin.getInstance().logger.error("Failed to fire Velocity event ${javaClass.name}", throwable)
    }
}