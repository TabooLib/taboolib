package taboolib.expansion

import taboolib.common.platform.function.submit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SynchronousRepeatChain<T>(
    override val block: Cancellable.() -> T,
    private val period: Long,
    private val delay: Long,
) : RepeatChainable<T> {

    override suspend fun execute(): T {
        return suspendCoroutine { cont ->
            val cancellable = Cancellable()
            submit(period = period, delay = delay) {
                val result = cancellable.call(block)
                if (cancellable.cancelled) {
                    cont.resume(result)
                    cancel()
                }
            }
        }
    }
}