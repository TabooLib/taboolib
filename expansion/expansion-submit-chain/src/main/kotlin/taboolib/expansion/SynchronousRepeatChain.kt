package taboolib.expansion

import kotlinx.coroutines.CompletableDeferred
import taboolib.common.platform.function.submit

class SynchronousRepeatChain<T>(
    override val block: () -> T,
    private val period: Long,
    private val delay: Long,
    private val predicate: () -> Boolean
) : Chainable<T> {

    override suspend fun execute(): T {
        val future = CompletableDeferred<T>()
        submit(period = period, delay = delay) {
            if (predicate()) {
                future.complete(block())
            }
        }
        return future.await()
    }
}