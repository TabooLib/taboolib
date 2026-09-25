package taboolib.common.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

class SyncExecutorTest {

    @Test
    fun `completeWith completes successful result`() {
        val future = CompletableFuture<Int>()

        future.completeWith { 42 }

        assertEquals(42, future.join())
    }

    @Test
    fun `completeWith propagates task failure`() {
        val future = CompletableFuture<Int>()
        val failure = IllegalStateException("boom")

        future.completeWith { throw failure }

        val thrown = assertThrows<CompletionException> { future.join() }
        assertSame(failure, thrown.cause)
    }
}
