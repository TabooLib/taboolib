package taboolib.expansion

import kotlinx.coroutines.suspendCancellableCoroutine
import taboolib.common.platform.service.PlatformExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface RepeatChainable<T> {

    val block: Cancellable.() -> T

    suspend fun execute(): T
}

internal suspend fun <T> executeRepeat(
    block: Cancellable.() -> T,
    submitTask: (PlatformExecutor.PlatformTask.() -> Unit) -> PlatformExecutor.PlatformTask,
): T {
    return suspendCancellableCoroutine { continuation ->
        val taskReference = AtomicReference<PlatformExecutor.PlatformTask?>()
        val completed = AtomicBoolean(false)
        val cancellable = Cancellable()
        continuation.invokeOnCancellation {
            completed.set(true)
            taskReference.get()?.cancel()
        }
        val task = try {
            submitTask {
                try {
                    val result = cancellable.call(block)
                    if (cancellable.cancelled && completed.compareAndSet(false, true)) {
                        cancel()
                        continuation.resume(result)
                    }
                } catch (ex: Throwable) {
                    cancel()
                    if (completed.compareAndSet(false, true)) {
                        continuation.resumeWithException(ex)
                    }
                }
            }
        } catch (ex: Throwable) {
            if (completed.compareAndSet(false, true)) {
                continuation.resumeWithException(ex)
            }
            return@suspendCancellableCoroutine
        }
        taskReference.set(task)
        // 覆盖竞态：任务可能在 taskReference 赋值之前就已完成（now = true 时同步执行），
        // 此时上面的完成回调取到的 taskReference 还是空，无法取消。
        // 这里补一次检查，cancel 是幂等的，重复调用无副作用。请勿删除。
        if (completed.get()) {
            task.cancel()
        }
    }
}
