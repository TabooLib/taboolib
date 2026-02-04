package taboolib.common.platform.event

import java.util.concurrent.CompletableFuture
import java.util.function.Function

/**
 * Hytale 事件处理器
 * @author sky
 */
sealed class HytaleEventHandler<T>(val priority: Short, val key: Any?) {

    /** 同步事件基类 */
    sealed class Sync<T>(priority: Short, key: Any?, val func: (T) -> Unit) : HytaleEventHandler<T>(priority, key)

    /** 异步事件基类 */
    sealed class AsyncBase<T>(priority: Short, key: Any?, val func: Function<CompletableFuture<T>, CompletableFuture<T>>) : HytaleEventHandler<T>(priority, key)

    /** ECS 事件基类 */
    sealed class Ecs<T, CTX>(val func: (CTX, T) -> Unit) : HytaleEventHandler<T>(0, null)

    /** 普通同步事件 (IBaseEvent<Void>) */
    class Normal<T>(priority: Short = 0, key: Any? = null, func: (T) -> Unit) : Sync<T>(priority, key, func)

    /** 全局同步事件 (订阅所有 key) */
    class Global<T>(priority: Short = 0, func: (T) -> Unit) : Sync<T>(priority, null, func)

    /** 未处理事件 */
    class Unhandled<T>(priority: Short = 0, func: (T) -> Unit) : Sync<T>(priority, null, func)

    /** 异步事件 (IAsyncEvent<Void>) */
    class Async<T>(priority: Short = 0, key: Any? = null, func: Function<CompletableFuture<T>, CompletableFuture<T>>) : AsyncBase<T>(priority, key, func)

    /** 异步全局事件 */
    class AsyncGlobal<T>(priority: Short = 0, func: Function<CompletableFuture<T>, CompletableFuture<T>>) : AsyncBase<T>(priority, null, func)

    /** 异步未处理事件 */
    class AsyncUnhandled<T>(priority: Short = 0, func: Function<CompletableFuture<T>, CompletableFuture<T>>) : AsyncBase<T>(priority, null, func)

    /** ECS 实体事件 (EcsEvent) */
    class EcsEntity<T, CTX>(func: (CTX, T) -> Unit) : Ecs<T, CTX>(func)
}
