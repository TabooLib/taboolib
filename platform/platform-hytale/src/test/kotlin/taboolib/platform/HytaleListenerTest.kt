package taboolib.platform

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class HytaleListenerTest {

    @Test
    fun `synchronous async handler failure becomes exceptional future`() {
        val source = CompletableFuture<String>()
        val failure = IllegalStateException("boom")

        val result = invokeAsyncHandler(source, Function { throw failure })
        var captured: Throwable? = null
        result.whenComplete { _, throwable -> captured = throwable }

        assertTrue(result.isCompletedExceptionally)
        assertSame(failure, captured)
    }

    @Test
    fun `cancelled future is returned without replacement`() {
        val source = CompletableFuture<String>()
        source.cancel(false)

        val result = invokeAsyncHandler(source, Function { it })

        assertSame(source, result)
        assertTrue(result.isCancelled)
    }

    @Test
    fun `handler result future is preserved`() {
        val source = CompletableFuture<String>()
        val transformed = CompletableFuture<String>()

        val result = invokeAsyncHandler(source, Function { transformed })

        assertSame(transformed, result)
    }
}
