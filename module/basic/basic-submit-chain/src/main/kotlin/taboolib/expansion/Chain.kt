package taboolib.expansion

import kotlinx.coroutines.*
import taboolib.common.platform.function.submit
import taboolib.expansion.DispatcherType.ASYNC
import taboolib.expansion.DispatcherType.SYNC
import taboolib.expansion.DurationType.MILLIS
import taboolib.expansion.DurationType.MINECRAFT_TICK
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

enum class DispatcherType {

    SYNC, ASYNC
}

enum class DurationType {

    MINECRAFT_TICK, MILLIS
}

object SyncDispatcher : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        submit(now = true) { block.run() }
    }
}

object AsyncDispatcher : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        submit(now = true, async = true) { block.run() }
    }
}

open class Chain<R>(val chain: suspend Chain<R>.() -> R) {

    suspend fun <T> async(block: () -> T): T {
        return withContext(AsyncDispatcher) {
            block()
        }
    }

    suspend fun <T> sync(block: () -> T): T {
        return withContext(SyncDispatcher) {
            block()
        }
    }

    suspend fun wait(value: Long, type: DurationType) {
        withContext(AsyncDispatcher) {
            when (type) {
                // 50ms = 1 tick in Minecraft
                MINECRAFT_TICK -> delay(value * 50)
                MILLIS -> value
            }
        }
    }

    suspend fun <T> sync(period: Long, now: Boolean = false, delay: Long = 0L, block: Cancellable.() -> T): T {
        return withContext(SyncDispatcher) {
            SynchronousRepeatChain(block, period, now, delay).execute()
        }
    }

    suspend fun <T> async(period: Long, now: Boolean = false, delay: Long = 0L, block: Cancellable.() -> T): T {
        return withContext(AsyncDispatcher) {
            AsynchronousRepeatChain(block, period, now, delay).execute()
        }
    }

    fun run(type: DispatcherType): CompletableFuture<R> {
        val future = CompletableFuture<R>()
        when (type) {
            SYNC -> CoroutineScope(SyncDispatcher).launch { future.complete(chain(this@Chain)) }
            ASYNC -> CoroutineScope(AsyncDispatcher).launch { future.complete(chain(this@Chain)) }
        }
        return future
    }
}

fun <R> chain(type: DispatcherType = ASYNC, block: suspend Chain<R>.() -> R): CompletableFuture<R> {
    return Chain(block).run(type)
}

fun <R> submitChain(type: DispatcherType = ASYNC, block: suspend Chain<R>.() -> R): CompletableFuture<R> {
    return Chain(block).run(type)
}