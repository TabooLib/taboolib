package taboolib.common.event

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class InternalEventBusConcurrencyTest {

    private class TestEvent : InternalEvent()

    @Test
    fun `concurrent listeners at the same priority are not lost`() {
        val threadCount = 24
        val executor = Executors.newFixedThreadPool(threadCount)
        val registered = CopyOnWriteArrayList<InternalListener>()
        try {
            repeat(50) { round ->
                val barrier = CyclicBarrier(threadCount)
                val calls = AtomicInteger()
                val futures = (0 until threadCount).map {
                    CompletableFuture.supplyAsync({
                        barrier.await(5, TimeUnit.SECONDS)
                        InternalEventBus.listen(TestEvent::class.java, Int.MIN_VALUE + round, false) {
                            calls.incrementAndGet()
                        }
                    }, executor)
                }
                futures.forEach { registered += it.get(10, TimeUnit.SECONDS) }

                InternalEventBus.call(TestEvent())

                assertEquals(threadCount, calls.get(), "round $round lost registered listeners")
                registered.forEach(InternalListener::cancel)
                registered.clear()
            }
        } finally {
            registered.forEach(InternalListener::cancel)
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }
    }
}
