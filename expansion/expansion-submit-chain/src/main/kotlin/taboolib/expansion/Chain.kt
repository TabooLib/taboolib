package taboolib.expansion

import kotlinx.coroutines.*
import taboolib.common.Isolated
import taboolib.common.platform.function.submit
import kotlin.coroutines.CoroutineContext


object SyncDispatcher : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        submit { block.run() }
    }
}

object AsyncDispatcher : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        submit(async = true) { block.run() }
    }
}

@Isolated
open class Chain(val chain: suspend Chain.() -> Unit) {

    suspend fun wait(value: Long) {
        withContext(AsyncDispatcher) {
            // 50ms = 1 tick in Minecraft
            delay(value * 50)
        }
    }

    suspend fun <T> sync(func: () -> T): T {
        return withContext(SyncDispatcher) {
            func()
        }
    }

    suspend fun <T> async(func: () -> T): T {
        return withContext(AsyncDispatcher) {
            func()
        }
    }

    suspend fun sync(period: Long, delay: Long = 0L, block: Cancellable.() -> Unit) {
        return withContext(SyncDispatcher) {
            SynchronousRepeatChain(block, period, delay).execute()
        }
    }

    suspend fun async(period: Long, delay: Long = 0L, block: Cancellable.() -> Unit) {
        return withContext(AsyncDispatcher) {
            AsynchronousRepeatChain(block, period, delay).execute()
        }
    }

    fun run() {
        CoroutineScope(AsyncDispatcher).launch {
            chain(this@Chain)
        }
    }
}

fun submitChain(block: suspend Chain.() -> Unit) {
    Chain(block).run()
}