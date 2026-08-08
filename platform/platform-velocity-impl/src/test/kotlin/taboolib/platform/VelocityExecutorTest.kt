package taboolib.platform

import com.velocitypowered.api.scheduler.ScheduledTask
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformExecutorState
import java.lang.reflect.Proxy
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

class VelocityExecutorTest {

    @Test
    fun `cancelled pending task never reads lateinit or gets scheduled`() {
        val scheduler = RecordingScheduler()
        val executor = executor(scheduler)
        val task = executor.submit(runnable())

        task.cancel()
        task.cancel()
        executor.start()

        assertEquals(0, scheduler.scheduled.size)
        assertEquals(0, executor.pendingTaskCount())
        assertEquals(0, executor.activeTaskCount())
    }

    @Test
    fun `cancellation before scheduled handle binding cancels handle exactly once`() {
        val scheduler = RecordingScheduler { task, _ -> task.cancel() }
        val executor = executor(scheduler)
        executor.start()

        val task = executor.submit(runnable())
        task.cancel()

        assertEquals(1, scheduler.scheduled.single().cancelCount)
        assertEquals(0, executor.activeTaskCount())
    }

    @Test
    fun `cancellation after scheduled handle binding is idempotent`() {
        val scheduler = RecordingScheduler()
        val executor = executor(scheduler)
        executor.start()

        val task = executor.submit(runnable())
        task.cancel()
        task.cancel()

        assertEquals(1, scheduler.scheduled.single().cancelCount)
        assertEquals(0, executor.activeTaskCount())
    }

    @Test
    fun `start moves pending tasks and stop is terminal`() {
        val scheduler = RecordingScheduler()
        val asyncExecutor = DirectExecutorService()
        val executor = executor(scheduler, asyncExecutor)
        executor.submit(runnable())

        assertEquals(PlatformExecutorState.NEW, executor.currentState())
        assertEquals(1, executor.pendingTaskCount())

        executor.start()
        assertEquals(PlatformExecutorState.RUNNING, executor.currentState())
        assertEquals(0, executor.pendingTaskCount())
        assertEquals(1, executor.activeTaskCount())

        executor.stop()
        executor.stop()

        assertEquals(PlatformExecutorState.STOPPED, executor.currentState())
        assertEquals(0, executor.activeTaskCount())
        assertEquals(1, scheduler.scheduled.single().cancelCount)
        assertEquals(1, asyncExecutor.shutdownNowCount)
        assertThrows(RejectedExecutionException::class.java) { executor.submit(runnable(now = true)) }
        assertThrows(RejectedExecutionException::class.java) { executor.submit(runnable()) }
    }

    @Test
    fun `stop continues cleanup when one scheduled cancellation fails`() {
        val scheduler = RecordingScheduler()
        val asyncExecutor = DirectExecutorService()
        val executor = executor(scheduler, asyncExecutor)
        val failure = IllegalStateException("cancel failed")
        executor.start()
        executor.submit(runnable())
        executor.submit(runnable())
        scheduler.scheduled.first().cancelFailure = failure

        val thrown = assertThrows(IllegalStateException::class.java) { executor.stop() }

        assertSame(failure, thrown)
        assertEquals(1, scheduler.scheduled.first().cancelCount)
        assertEquals(1, scheduler.scheduled.last().cancelCount)
        assertEquals(1, asyncExecutor.shutdownNowCount)
        assertEquals(PlatformExecutorState.STOPPED, executor.currentState())
        assertEquals(0, executor.activeTaskCount())
    }

    @Test
    fun `now task queued before start runs without velocity scheduler`() {
        val scheduler = RecordingScheduler()
        val executor = executor(scheduler)
        var executions = 0
        executor.submit(runnable(now = true) { executions++ })

        executor.start()

        assertEquals(1, executions)
        assertTrue(scheduler.scheduled.isEmpty())
        assertEquals(0, executor.activeTaskCount())
    }

    @Test
    fun `user exception is reported and rethrown`() {
        val scheduler = RecordingScheduler()
        val reports = ArrayList<Throwable>()
        val executor = executor(scheduler, reporter = reports::add)
        val failure = IllegalStateException("boom")
        executor.start()
        executor.submit(runnable(async = true) { throw failure })

        val thrown = assertThrows(IllegalStateException::class.java) {
            scheduler.scheduled.single().action.run()
        }

        assertSame(failure, thrown)
        assertEquals(listOf(failure), reports)
        assertEquals(0, executor.activeTaskCount())
    }

    @Test
    fun `public task contract keeps scheduled task field`() {
        val runningTask = VelocityExecutor.VelocityRunningTask::class.java

        assertEquals(ScheduledTask::class.java, runningTask.getField("scheduledTask").type)
        assertEquals(ScheduledTask::class.java, runningTask.getMethod("getScheduledTask").returnType)
        VelocityExecutor::class.java.getDeclaredConstructor()
    }

    @Test
    fun `async dispatch rejection reports failure and completes task`() {
        val scheduler = RecordingScheduler()
        val failure = RejectedExecutionException("dispatch rejected")
        val reports = ArrayList<Throwable>()
        val executor = executor(scheduler, RejectingExecutorService(failure)) { reports.add(it) }
        executor.start()
        executor.submit(runnable(async = true))

        val thrown = assertThrows(RejectedExecutionException::class.java) {
            scheduler.scheduled.single().action.run()
        }

        assertSame(failure, thrown)
        assertEquals(listOf(failure), reports)
        assertEquals(1, scheduler.scheduled.single().cancelCount)
        assertEquals(0, executor.activeTaskCount())
    }

    @Test
    fun `async thread names use velocity prefix`() {
        val factory = VelocityAsyncThreadFactory()

        val first = factory.newThread {}
        val second = factory.newThread {}

        assertEquals("TabooLib-Velocity-Async-1", first.name)
        assertEquals("TabooLib-Velocity-Async-2", second.name)
        assertFalse(first.isAlive)
        assertFalse(second.isAlive)
    }

    private fun executor(
        scheduler: RecordingScheduler,
        asyncExecutor: ExecutorService = DirectExecutorService(),
        reporter: (Throwable) -> Unit = {},
    ): VelocityExecutor {
        return VelocityExecutor(scheduler, asyncExecutor, reporter, false)
    }

    private fun runnable(
        now: Boolean = false,
        async: Boolean = false,
        delay: Long = 0,
        period: Long = 0,
        block: PlatformExecutor.PlatformTask.() -> Unit = {},
    ): PlatformExecutor.PlatformRunnable {
        return PlatformExecutor.PlatformRunnable(now, async, delay, period, block)
    }

    private class RecordingScheduler(
        private val beforeReturn: (VelocityExecutor.VelocityRunningTask, RecordedTask) -> Unit = { _, _ -> },
    ) : VelocityTaskScheduler {

        val scheduled = ArrayList<RecordedTask>()

        override fun schedule(
            task: VelocityExecutor.VelocityRunningTask,
            runnable: PlatformExecutor.PlatformRunnable,
            action: Runnable,
        ): ScheduledTask {
            val recorded = RecordedTask(action)
            scheduled += recorded
            beforeReturn(task, recorded)
            return recorded.handle
        }
    }

    private class RecordedTask(val action: Runnable) {

        var cancelCount = 0
        var cancelFailure: Throwable? = null

        val handle: ScheduledTask = Proxy.newProxyInstance(
            ScheduledTask::class.java.classLoader,
            arrayOf(ScheduledTask::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "cancel" -> {
                    cancelCount++
                    cancelFailure?.let { throw it }
                    null
                }
                "toString" -> "RecordedScheduledTask"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.get(0)
                else -> null
            }
        } as ScheduledTask
    }

    private class RejectingExecutorService(private val failure: RejectedExecutionException) : AbstractExecutorService() {

        override fun shutdown() = Unit

        override fun shutdownNow(): MutableList<Runnable> = ArrayList()

        override fun isShutdown(): Boolean = false

        override fun isTerminated(): Boolean = false

        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = false

        override fun execute(command: Runnable) {
            throw failure
        }
    }

    private class DirectExecutorService : AbstractExecutorService() {

        private var shutdown = false
        var shutdownNowCount = 0

        override fun shutdown() {
            shutdown = true
        }

        override fun shutdownNow(): MutableList<Runnable> {
            shutdown = true
            shutdownNowCount++
            return ArrayList()
        }

        override fun isShutdown(): Boolean = shutdown

        override fun isTerminated(): Boolean = shutdown

        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = shutdown

        override fun execute(command: Runnable) {
            if (shutdown) {
                throw RejectedExecutionException()
            }
            command.run()
        }
    }
}
