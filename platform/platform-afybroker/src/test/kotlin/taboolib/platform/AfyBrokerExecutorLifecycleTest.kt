package taboolib.platform

import net.afyer.afybroker.server.scheduler.ScheduledTask
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.common.platform.service.PlatformExecutor
import java.io.Closeable
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicReference

class AfyBrokerExecutorLifecycleTest {

    @Test
    fun `cancel before binding cancels delegate exactly once`() {
        val delegate = ManualDelegate()
        val cancellation = AfyBrokerTaskCancellation<ManualDelegate> { it.cancel() }

        assertTrue(cancellation.cancel())
        assertFalse(cancellation.cancel())
        cancellation.bind(delegate)
        cancellation.bind(delegate)

        assertEquals(1, delegate.cancelCount)
        assertTrue(cancellation.isCancelled())
    }

    @Test
    fun `cancellation cleanup runs even when delegate throws`() {
        val failure = IllegalStateException("cancel failed")
        val delegate = ManualDelegate()
        val cancellation = AfyBrokerTaskCancellation<ManualDelegate> { throw failure }
        var cleanupCount = 0
        cancellation.bind(delegate)

        val thrown = assertThrows(IllegalStateException::class.java) {
            cancellation.cancel { cleanupCount++ }
        }

        assertSame(failure, thrown)
        assertEquals(1, cleanupCount)
        assertTrue(cancellation.isCancelled())
        assertFalse(cancellation.cancel { cleanupCount++ })
        assertEquals(1, cleanupCount)
    }

    @Test
    fun `platform task cancel is idempotent`() {
        var cancelCount = 0
        val task = AfyBrokerExecutor.BrokerPlatformTask(Closeable { cancelCount++ })

        task.cancel()
        task.cancel()

        assertEquals(1, cancelCount)
    }

    @Test
    fun `pending task cancel does not access unbound scheduled task`() {
        val runningTask = AfyBrokerExecutor.AfyBrokerRunningTask(
            PlatformExecutor.PlatformRunnable(false, false, 0, 0) {}
        )

        assertDoesNotThrow { runningTask.platformTask().cancel() }
    }

    @Test
    fun `binding before cancel is safe and idempotent`() {
        val delegate = ManualDelegate()
        val cancellation = AfyBrokerTaskCancellation<ManualDelegate> { it.cancel() }

        cancellation.bind(delegate)
        assertEquals(0, delegate.cancelCount)
        assertTrue(cancellation.cancel())
        assertFalse(cancellation.cancel())

        assertEquals(1, delegate.cancelCount)
    }

    @Test
    fun `cancelled task gate rejects later execution`() {
        val cancellation = AfyBrokerTaskCancellation<ManualDelegate> { it.cancel() }
        var executions = 0

        cancellation.cancel()

        assertFalse(cancellation.runIfActive { executions++ })
        assertEquals(0, executions)
    }

    @Test
    fun `registry moves pending tasks to active and completes them`() {
        val registry = AfyBrokerTaskRegistry<String>()

        assertEquals(AfyBrokerTaskRegistration.PENDING, registry.register("pending"))
        assertEquals(AfyBrokerExecutorState.NEW, registry.state())
        assertEquals(1, registry.pendingCount())

        assertEquals(listOf("pending"), registry.start())
        assertEquals(AfyBrokerExecutorState.RUNNING, registry.state())
        assertEquals(0, registry.pendingCount())
        assertEquals(1, registry.activeCount())
        assertEquals(AfyBrokerTaskRegistration.ACTIVE, registry.register("active"))
        assertTrue(registry.remove("pending"))
        assertEquals(1, registry.activeCount())
    }

    @Test
    fun `stop drains pending and active tasks then rejects submissions`() {
        val registry = AfyBrokerTaskRegistry<String>()
        registry.register("pending")
        registry.start()
        registry.register("active")

        assertEquals(listOf("pending", "active"), registry.stop())
        assertEquals(AfyBrokerExecutorState.STOPPED, registry.state())
        assertEquals(0, registry.pendingCount())
        assertEquals(0, registry.activeCount())
        assertEquals(AfyBrokerTaskRegistration.REJECTED, registry.register("late"))
        assertTrue(registry.stop().isEmpty())
        assertTrue(registry.start().isEmpty())
    }

    @Test
    fun `stopped executor rejects now and scheduled submissions`() {
        val executor = AfyBrokerExecutor()
        executor.stop()

        assertThrows(RejectedExecutionException::class.java) {
            executor.submit(PlatformExecutor.PlatformRunnable(true, false, 0, 0) {})
        }
        assertThrows(RejectedExecutionException::class.java) {
            executor.submit(PlatformExecutor.PlatformRunnable(false, false, 0, 0) {})
        }
    }

    @Test
    fun `async dispatch failure reports and cleans up without replacing failure`() {
        val failure = RejectedExecutionException("dispatch rejected")
        val cleanupFailure = IllegalStateException("cleanup failed")
        var reported: Throwable? = null
        var cleanupCount = 0

        val thrown = assertThrows(RejectedExecutionException::class.java) {
            runAfyBrokerDispatch(
                reporter = { reported = it },
                cleanup = {
                    cleanupCount++
                    throw cleanupFailure
                },
            ) {
                throw failure
            }
        }

        assertSame(failure, thrown)
        assertSame(failure, reported)
        assertEquals(1, cleanupCount)
        assertEquals(listOf(cleanupFailure), failure.suppressed.toList())
    }

    @Test
    fun `user task failure is reported and rethrown unchanged`() {
        val failure = IllegalStateException("boom")
        var reported: Throwable? = null

        val thrown = assertThrows(IllegalStateException::class.java) {
            runAfyBrokerTask({ reported = it }) {
                throw failure
            }
        }

        assertSame(failure, reported)
        assertSame(failure, thrown)
    }

    @Test
    fun `reporter failure is suppressed without replacing user failure`() {
        val failure = IllegalStateException("user")
        val reporterFailure = IllegalArgumentException("reporter")

        val thrown = assertThrows(IllegalStateException::class.java) {
            runAfyBrokerTask({ throw reporterFailure }) {
                throw failure
            }
        }

        assertSame(failure, thrown)
        assertEquals(listOf(reporterFailure), thrown.suppressed.toList())
    }

    @Test
    fun `active gate prevents callback after disable`() {
        val gate = AfyBrokerActiveGate()
        var activeCalls = 0

        assertTrue(gate.close().isDone)
        assertFalse(gate.activate { activeCalls++ })
        assertEquals(0, activeCalls)
    }

    @Test
    fun `active gate defers disable continuation without blocking`() {
        val gate = AfyBrokerActiveGate()
        val order = ArrayList<String>()
        val closed = AtomicReference<java.util.concurrent.CompletableFuture<Void>>()

        assertTrue(gate.activate {
            order += "active-start"
            closed.set(gate.close())
            assertFalse(closed.get().isDone)
            closed.get().thenRun { order += "disable" }
            order += "active-end"
        })

        assertTrue(closed.get().isDone)
        assertEquals(listOf("active-start", "active-end", "disable"), order)
        assertFalse(gate.activate { order += "late-active" })
    }

    @Test
    fun `public executor contract remains compatible`() {
        val executorClass = AfyBrokerExecutor::class.java
        val runningTaskClass = AfyBrokerExecutor.AfyBrokerRunningTask::class.java

        executorClass.getDeclaredConstructor()
        assertTrue(PlatformExecutor::class.java.isAssignableFrom(executorClass))
        assertEquals(ScheduledTask::class.java, runningTaskClass.getField("scheduledTask").type)
        assertEquals(ScheduledTask::class.java, runningTaskClass.getMethod("getScheduledTask").returnType)
        assertEquals(
            PlatformExecutor.PlatformTask::class.java,
            runningTaskClass.getMethod("platformTask").returnType
        )
    }

    private class ManualDelegate {

        var cancelCount = 0
            private set

        fun cancel() {
            cancelCount++
        }
    }
}
