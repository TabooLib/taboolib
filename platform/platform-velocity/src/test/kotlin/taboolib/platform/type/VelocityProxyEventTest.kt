package taboolib.platform.type

import com.velocitypowered.api.event.ResultedEvent.GenericResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

class VelocityProxyEventTest {

    @Test
    fun `callAsync completes with final cancellation state`() {
        val fired = CompletableFuture<VelocityProxyEvent>()
        val event = TestEvent(fired)

        val result = event.callAsync()

        assertEquals(1, event.fireCount)
        assertFalse(result.isDone)

        event.result = GenericResult.denied()
        fired.complete(event)

        assertFalse(result.getNow(true))
    }

    @Test
    fun `callAsync propagates exceptional completion`() {
        val fired = CompletableFuture<VelocityProxyEvent>()
        val event = TestEvent(fired)
        val failure = IllegalStateException("fire failed")

        val result = event.callAsync()
        fired.completeExceptionally(failure)

        val thrown = assertThrows(CompletionException::class.java) {
            result.getNow(true)
        }
        assertSame(failure, thrown.cause)
    }

    @Test
    fun `call returns current snapshot without waiting for unfinished fire`() {
        val fired = CompletableFuture<VelocityProxyEvent>()
        val event = TestEvent(fired)

        assertTrue(event.call())
        assertEquals(1, event.fireCount)
        assertFalse(fired.isDone)

        event.result = GenericResult.denied()
        fired.complete(event)
    }

    @Test
    fun `call returns final state when fire completes synchronously`() {
        lateinit var event: TestEvent
        event = TestEvent {
            event.result = GenericResult.denied()
            CompletableFuture.completedFuture(event)
        }

        assertFalse(event.call())
        assertEquals(1, event.fireCount)
    }

    @Test
    fun `call observes later asynchronous failure`() {
        val fired = CompletableFuture<VelocityProxyEvent>()
        val event = TestEvent(fired)
        val failure = IllegalArgumentException("listener failed")

        assertTrue(event.call())
        fired.completeExceptionally(failure)

        assertSame(failure, event.observedFailure)
    }

    @Test
    fun `call contains reporter failures without losing original failure`() {
        val fired = CompletableFuture<VelocityProxyEvent>()
        val reporterFailure = IllegalStateException("reporter failed")
        val event = TestEvent(fired).also { it.reporterFailure = reporterFailure }
        val failure = IllegalArgumentException("listener failed")

        assertTrue(event.call())
        fired.completeExceptionally(failure)

        assertSame(failure, event.observedFailure)
        assertEquals(listOf(reporterFailure), failure.suppressed.toList())
    }

    @Test
    fun `call keeps primitive boolean JVM signature`() {
        val method = VelocityProxyEvent::class.java.getDeclaredMethod("call")

        assertEquals(Boolean::class.javaPrimitiveType, method.returnType)
        assertEquals(0, method.parameterCount)
    }

    private class TestEvent(
        private val fire: () -> CompletableFuture<VelocityProxyEvent>
    ) : VelocityProxyEvent() {

        constructor(future: CompletableFuture<VelocityProxyEvent>) : this({ future })

        var fireCount = 0
        var observedFailure: Throwable? = null
        var reporterFailure: Throwable? = null

        override fun fireEvent(): CompletableFuture<VelocityProxyEvent> {
            fireCount++
            return fire()
        }

        override fun onCallFailure(throwable: Throwable) {
            observedFailure = throwable
            reporterFailure?.let { throw it }
        }
    }
}
