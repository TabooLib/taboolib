package taboolib.module.kether

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import taboolib.common.OpenContainer
import taboolib.common.OpenResult
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport

class RemoteQuestReaderTest {

    @Test
    fun `reader operations are serialized per remote source`() {
        val source = ConcurrentReaderSource()
        val reader = RemoteQuestReader(TestContainer, source)
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        try {
            val tasks = List(32) {
                executor.submit<String> {
                    start.await()
                    reader.nextToken()
                }
            }
            start.countDown()
            tasks.forEach { future ->
                assertEquals("token", future.get(5, TimeUnit.SECONDS))
            }
        } finally {
            executor.shutdownNow()
        }

        assertEquals(1, source.maxConcurrentCalls.get())
    }

    @Test
    fun `readers wrapping the same source are mutually exclusive`() {
        // 锁对象若是 Reader 实例，两个包装同一 source 的 Reader 之间不会互斥
        val source = ConcurrentReaderSource()
        val readers = List(4) { RemoteQuestReader(TestContainer, source) }
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        try {
            val tasks = List(32) { index ->
                executor.submit<String> {
                    start.await()
                    readers[index % readers.size].nextToken()
                }
            }
            start.countDown()
            tasks.forEach { future ->
                assertEquals("token", future.get(5, TimeUnit.SECONDS))
            }
        } finally {
            executor.shutdownNow()
        }

        assertEquals(1, source.maxConcurrentCalls.get())
    }

    private class ConcurrentReaderSource {

        private val activeCalls = AtomicInteger()
        val maxConcurrentCalls = AtomicInteger()

        fun nextToken(): String {
            val active = activeCalls.incrementAndGet()
            maxConcurrentCalls.updateAndGet { current -> maxOf(current, active) }
            try {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(2))
                return "token"
            } finally {
                activeCalls.decrementAndGet()
            }
        }
    }

    private object TestContainer : OpenContainer {

        override fun isValid() = true

        override fun getName() = "test"

        override fun call(name: String, args: Array<out Any>) = OpenResult.failed()
    }
}
