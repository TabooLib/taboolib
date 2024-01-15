package taboolib.expansion

import kotlinx.coroutines.*
import taboolib.common.Isolated
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
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

@RuntimeDependencies(
    RuntimeDependency(
        "!org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4",
        test = "!kotlinx.coroutines.GlobalScope",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@.", "!kotlinx.", "!kotlinx_1_6_4."],
        transitive = false
    )
)
class ChainEnv

@Isolated
open class Chain(val block: suspend Chain.() -> Unit) {

    suspend fun wait(value: Long) {
        withContext(chainDispatch) {
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
            SynchronizeRepeatTask(block, period, delay, predicate).execute()
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