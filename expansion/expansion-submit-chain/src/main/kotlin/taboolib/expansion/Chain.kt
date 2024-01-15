package taboolib.expansion

import kotlinx.coroutines.*
import taboolib.common.Isolated
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


val CHAIN_EXECUTORS = Executors.newScheduledThreadPool(8)

@OptIn(InternalCoroutinesApi::class)
class ExecutorDispatch : CoroutineDispatcher(), Delay {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        CHAIN_EXECUTORS.submit(block)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        CHAIN_EXECUTORS.schedule({
            with(continuation) {
                resumeUndispatched(Unit)
            }
        }, timeMillis, TimeUnit.MILLISECONDS)
    }
}

val chainDispatch = ExecutorDispatch()

@Isolated
open class Chain(val block: suspend Chain.() -> Unit) {

    suspend fun wait(value: Long) {
        withContext(chainDispatch) {
            // 50ms = 1 tick in Minecraft
            delay(value * 50)
        }
    }

    suspend fun <T> sync(func: () -> T): T {
        return withContext(chainDispatch) {
            SynchronousChain(func).execute()
        }
    }

    suspend fun <T> async(func: () -> T): T {
        return withContext(chainDispatch) {
            AsynchronousChain(func).execute()
        }
    }

    suspend fun <T> sync(predicate: () -> Boolean, period: Long, delay: Long, block: () -> T): T {
        return withContext(chainDispatch) {
            SynchronousRepeatChain(block, period, delay, predicate).execute()
        }
    }

    suspend fun <T> async(predicate: () -> Boolean, period: Long, delay: Long, block: () -> T): T {
        return withContext(chainDispatch) {
            AsynchronousRepeatChain(block, period, delay, predicate).execute()
        }
    }

    fun run() {
        CoroutineScope(chainDispatch).launch {
            block(this@Chain)
        }
    }
}

fun submitChain(block: suspend Chain.() -> Unit) {
    Chain(block).run()
}