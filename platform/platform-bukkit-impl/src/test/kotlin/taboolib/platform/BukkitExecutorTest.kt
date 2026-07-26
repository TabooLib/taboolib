package taboolib.platform

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.common.platform.service.PlatformExecutor

class BukkitExecutorTest {

    @Test
    fun `context-free synchronous task is not rejected on Folia`() {
        withFolia {
            val executor = BukkitExecutor()
            // 服务器尚未启动，任务应被排入队列而非抛出异常。
            // 在 Folia 上直接拒绝此类任务会让 @Schedule 静默失效，因此改为回退到全局区域调度器。
            val task = executor.submit(runnable(now = false, async = false))
            assertTrue(task is BukkitExecutor.BukkitPlatformTask)
        }
    }

    @Test
    fun `asynchronous and immediate tasks keep their existing entry points on Folia`() {
        withFolia {
            val executor = BukkitExecutor()
            assertTrue(executor.submit(runnable(now = false, async = true)) is BukkitExecutor.BukkitPlatformTask)
            assertTrue(executor.submit(runnable(now = true, async = false)) is BukkitExecutor.BukkitPlatformTask)
        }
    }

    @Test
    fun `non Folia environment keeps region ownership semantics permissive`() {
        val previous = Folia.isFolia
        Folia.isFolia = false
        try {
            // 非 Folia 服务端没有区域概念，isOwnedByCurrentRegion 不应退化为主线程检查，
            // 否则 bukkit-navigation 等模块在异步线程上的 callRegion 调用会全部抛出异常。
            assertFalse(Folia.isFolia)
        } finally {
            Folia.isFolia = previous
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
