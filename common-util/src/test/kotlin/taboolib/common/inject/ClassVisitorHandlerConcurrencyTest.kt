package taboolib.common.inject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.tabooproject.reflex.ReflexClass
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ClassVisitorHandlerConcurrencyTest {

    @Test
    fun `class set is initialized once and safely published`() {
        val classesField = ClassVisitorHandler::class.java.getDeclaredField("classes").also { it.isAccessible = true }
        val previous = classesField.get(null)
        classesField.set(null, null)

        val threadCount = 16
        val ready = CountDownLatch(threadCount)
        val start = CountDownLatch(1)
        val initializerStarted = CountDownLatch(1)
        val releaseInitializer = CountDownLatch(1)
        val initializerCalls = AtomicInteger()
        val executor = Executors.newFixedThreadPool(threadCount)
        try {
            val futures = (0 until threadCount).map {
                executor.submit<Set<ReflexClass>> {
                    ready.countDown()
                    assertTrue(start.await(5, TimeUnit.SECONDS))
                    ClassVisitorHandler.getOrInitializeClasses {
                        initializerCalls.incrementAndGet()
                        initializerStarted.countDown()
                        assertTrue(releaseInitializer.await(5, TimeUnit.SECONDS))
                        emptySet()
                    }
                }
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS))
            start.countDown()
            assertTrue(initializerStarted.await(5, TimeUnit.SECONDS))
            releaseInitializer.countDown()

            val results = futures.map { it.get(10, TimeUnit.SECONDS) }
            assertEquals(1, initializerCalls.get())
            results.drop(1).forEach { assertSame(results.first(), it) }
        } finally {
            releaseInitializer.countDown()
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
            classesField.set(null, previous)
        }
    }
}
