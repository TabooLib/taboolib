package taboolib.expansion

import kotlinx.coroutines.CompletableDeferred
import taboolib.common.platform.function.submit

class SynchronousRepeatChain(
    override val block: Cancellable.() -> Unit,
    private val period: Long,
    private val delay: Long,
) : RepeatChainable {

    override suspend fun execute() {
        val future = CompletableDeferred<Unit>()
        val cancellable = Cancellable()
        submit(period = period, delay = delay) {
            cancellable.apply(block)
            if (cancellable.cancelled) {
                future.complete(Unit)
                cancel()
            }
        }
        return future.await()
    }
}