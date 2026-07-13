package taboolib.expansion

import taboolib.common.platform.function.submit

class SynchronousRepeatChain<T>(
    override val block: Cancellable.() -> T,
    private val period: Long,
    private val now: Boolean,
    private val delay: Long,
) : RepeatChainable<T> {

    override suspend fun execute(): T {
        return executeRepeat(block) { executor ->
            submit(period = period, now = now, delay = delay, executor = executor)
        }
    }
}
