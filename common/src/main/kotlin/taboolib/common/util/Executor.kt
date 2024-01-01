@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common.util

import taboolib.common.Isolated
import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import java.util.concurrent.CompletableFuture

/**
 * 在异步线程执行一个同步任务，并等待其完成
 *
 * @throws IllegalStateException 如果当前线程为主线程
 * @return 任务返回值
 */
inline fun <T> sync(noinline func: () -> T): T {
    if (isPrimaryThread) {
        error("Cannot run sync task in main thread.")
    }
    val future = CompletableFuture<T>()
    submit { future.complete(func()) }
    return future.get()
}