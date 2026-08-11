package taboolib.platform

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import taboolib.common.platform.service.PlatformExecutor

class BukkitExecutorTest {

    @Test
    fun `context-free synchronous task is rejected before enqueue on Folia`() {
        withFolia {
            val executor = BukkitExecutor()
            assertThrows(IllegalStateException::class.java) {
                executor.submit(runnable(now = false, async = false))
            }
        }
    }

    @Test
    fun `asynchronous and immediate tasks keep their existing entry points on Folia`() {
        withFolia {
            val executor = BukkitExecutor()
            assertDoesNotThrow { executor.submit(runnable(now = false, async = true)) }
            assertDoesNotThrow { executor.submit(runnable(now = true, async = false)) }
        }
    }

    @Test
    fun `Folia running task cannot bypass synchronous context check`() {
        withFolia {
            val task = BukkitExecutor.FoliaRunningTask(runnable(now = false, async = false))
            assertThrows(IllegalStateException::class.java) {
                task.execute(async = false, delay = 0, period = 0)
            }
        }
    }

    private fun runnable(now: Boolean, async: Boolean): PlatformExecutor.PlatformRunnable {
        return PlatformExecutor.PlatformRunnable(now, async, 0, 0) {}
    }

    private fun withFolia(block: () -> Unit) {
        val previous = Folia.isFolia
        Folia.isFolia = true
        try {
            block()
        } finally {
            Folia.isFolia = previous
        }
    }
}
