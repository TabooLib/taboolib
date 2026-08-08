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
    fun `command dispatch reports synchronous failure without throwing`() {
        // 命令派发失败不应打断调用方：异常被记录而非抛出，返回值仍为 true。
        assertTrue(HytaleCommandSender.dispatchCommand { throw IllegalStateException("boom") })
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
        var calls = 0
        HytaleCommandSender.registerQuitCallback(session, Runnable { throw IllegalStateException("boom") })
        HytaleCommandSender.registerQuitCallback(session, Runnable { calls++ })

        // 该方法由平台事件回调触发，回调异常仅记录不抛出，避免中断后续监听器
        HytaleCommandSender.fireQuitCallbacks(session)

        assertEquals(1, calls)
    }

    @Test
    fun `registering while online clears the completed session marker`() {
        val session = Any()
        var calls = 0
        HytaleCommandSender.fireQuitCallbacks(session)

        // 玩家在线说明是新会话，遗留的「已完成」标记应被清除，回调转为等待而非立即执行
        HytaleCommandSender.registerQuitCallback(session, Runnable { calls++ }, online = true)
        assertEquals(0, calls)

        HytaleCommandSender.fireQuitCallbacks(session)
        assertEquals(1, calls)
    }
}
