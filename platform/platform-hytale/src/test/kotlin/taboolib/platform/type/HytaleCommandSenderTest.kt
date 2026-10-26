package taboolib.platform.type

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

class HytaleCommandSenderTest {

    @Test
    fun `command dispatch never waits for pending future`() {
        val pending = CompletableFuture<Void>()

        assertTrue(HytaleCommandSender.dispatchCommand { pending })
    }

    @Test
    fun `command dispatch reports successful submission regardless of later state`() {
        val cancelled = CompletableFuture<Void>()
        cancelled.cancel(false)
        val failed = CompletableFuture<Void>()
        failed.completeExceptionally(IllegalStateException("boom"))

        assertTrue(HytaleCommandSender.dispatchCommand { cancelled })
        assertTrue(HytaleCommandSender.dispatchCommand { failed })
    }

    @Test
    fun `command dispatch propagates synchronous failure`() {
        val failure = IllegalStateException("boom")

        val thrown = assertThrows(IllegalStateException::class.java) {
            HytaleCommandSender.dispatchCommand { throw failure }
        }

        assertSame(failure, thrown)
    }

    @Test
    fun `quit callbacks run once across wrapper instances`() {
        val session = Any()
        var calls = 0
        HytaleCommandSender.registerQuitCallback(session, Runnable { calls++ })
        HytaleCommandSender.registerQuitCallback(session, Runnable { calls++ })

        HytaleCommandSender.fireQuitCallbacks(session)
        HytaleCommandSender.fireQuitCallbacks(session)

        assertEquals(2, calls)
    }

    @Test
    fun `late quit registration runs immediately until a new session activates`() {
        val session = Any()
        var calls = 0
        HytaleCommandSender.fireQuitCallbacks(session)

        HytaleCommandSender.registerQuitCallback(session, Runnable { calls++ })
        HytaleCommandSender.activateQuitSession(session)
        HytaleCommandSender.registerQuitCallback(session, Runnable { calls++ })
        HytaleCommandSender.fireQuitCallbacks(session)

        assertEquals(2, calls)
    }

    @Test
    fun `clearing quit callbacks releases pending registrations`() {
        val session = Any()
        var calls = 0
        HytaleCommandSender.registerQuitCallback(session, Runnable { calls++ })

        HytaleCommandSender.clearQuitCallbacks()
        HytaleCommandSender.fireQuitCallbacks(session)

        assertEquals(0, calls)
    }

    @Test
    fun `quit callback failure does not skip remaining callbacks`() {
        val session = Any()
        val failure = IllegalStateException("boom")
        var calls = 0
        HytaleCommandSender.registerQuitCallback(session, Runnable { throw failure })
        HytaleCommandSender.registerQuitCallback(session, Runnable { calls++ })

        val thrown = assertThrows(IllegalStateException::class.java) {
            HytaleCommandSender.fireQuitCallbacks(session)
        }

        assertSame(failure, thrown)
        assertEquals(1, calls)
    }
}
