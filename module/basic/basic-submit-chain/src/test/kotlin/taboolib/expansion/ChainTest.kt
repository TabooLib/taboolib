package taboolib.expansion

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CompletionException

class ChainTest {

    @Test
    fun `successful chain completes future`() {
        val future = Chain { 42 }.run(Dispatchers.Unconfined)

        assertEquals(42, future.join())
    }

    @Test
    fun `failed chain completes future exceptionally`() {
        val failure = IllegalStateException("boom")
        val future = Chain<Int> { throw failure }.run(Dispatchers.Unconfined)

        val thrown = assertThrows<CompletionException> { future.join() }
        assertSame(failure, thrown.cause)
    }

    @Test
    fun `future cancellation cancels running chain`() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val stopped = CompletableDeferred<Unit>()
        val future = Chain<Int> {
            try {
                started.complete(Unit)
                awaitCancellation()
            } finally {
                stopped.complete(Unit)
            }
        }.run(Dispatchers.Default)

        started.await()
        assertTrue(future.cancel(false))
        withTimeout(5_000) {
            stopped.await()
        }
        assertTrue(future.isCancelled)
    }
}
