package taboolib.expansion

import kotlinx.coroutines.CompletableDeferred
import taboolib.common.platform.function.submit

class SynchronousChain<T>(override val block: () -> T) : Chainable<T> {

    override suspend fun execute(): T {
        val future = CompletableDeferred<T>()
        submit {
            future.complete(block())
        }
        return future.await()
    }
}