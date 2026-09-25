package taboolib.expansion

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import taboolib.common.platform.service.PlatformExecutor

class RepeatChainTest {

    @Test
    fun `repeat chain resumes when block cancels itself`() = runBlocking {
        val task = TestTask()

        val result = executeRepeat({
            cancel()
            42
        }) { executor ->
            task.executor()
            task
        }

        assertEquals(42, result)
        assertTrue(task.cancelled)
    }

    @Test
    fun `repeat chain propagates callback failure`() {
        val failure = IllegalStateException("boom")
        val task = TestTask()

        val thrown = assertThrows<IllegalStateException> {
            runBlocking {
                executeRepeat<Int>({ throw failure }) { executor ->
                    task.executor()
                    task
                }
            }
        }

        assertTrue(thrown === failure || thrown.cause === failure)
        assertTrue(task.cancelled)
    }

    @Test
    fun `coroutine cancellation cancels scheduled task`() = runBlocking {
        val task = TestTask()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            executeRepeat({ Unit }) { task }
        }

        job.cancelAndJoin()

        assertTrue(task.cancelled)
    }

    @Test
    fun `scheduler submission failure is propagated`() {
        val failure = IllegalStateException("scheduler unavailable")

        val thrown = assertThrows<IllegalStateException> {
            runBlocking {
                executeRepeat({ Unit }) { throw failure }
            }
        }

        assertTrue(thrown === failure || thrown.cause === failure)
    }

    private class TestTask : PlatformExecutor.PlatformTask {

        var cancelled = false
            private set

        override fun cancel() {
            cancelled = true
        }
    }
}
