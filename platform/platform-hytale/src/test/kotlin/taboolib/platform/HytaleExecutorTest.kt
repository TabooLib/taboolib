package taboolib.platform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.common.platform.service.PlatformExecutor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.util.ArrayDeque
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class HytaleExecutorTest {

    @Test
    fun `cancelled pending task never reaches scheduler`() {
        val scheduler = RecordingScheduler()
        val executor = executor(scheduler)
        val task = executor.submit(runnable())

        task.cancel()
        executor.start()

        assertEquals(0, scheduler.scheduleCount)
    }

    @Test
    fun `synchronous scheduled action propagates and reports user exception`() {
        val scheduler = RecordingScheduler()
        val failures = ArrayList<Throwable>()
        val executor = executor(scheduler, failures = failures)
        val failure = IllegalStateException("boom")
        executor.start()
        executor.submit(runnable { throw failure })

        val thrown = assertThrows(IllegalStateException::class.java) {
            scheduler.action.run()
        }

        assertSame(failure, thrown)
        assertEquals(listOf(failure), failures)
    }

    @Test
    fun `async task remains offloaded from scheduler action`() {
        val scheduler = RecordingScheduler()
        val async = RecordingExecutorService()
        val executor = executor(scheduler, async)
        var calls = 0
        executor.start()
        executor.submit(runnable(async = true) { calls++ })

        scheduler.action.run()

        assertEquals(0, calls)
        assertEquals(1, async.queuedTaskCount)
        async.runNext()
        assertEquals(1, calls)
    }

    @Test
    fun `periodic synchronous failure removes active task`() {
        val scheduler = RecordingScheduler()
        val failures = ArrayList<Throwable>()
        val executor = executor(scheduler, failures = failures)
        val failure = IllegalStateException("boom")
        executor.start()
        executor.submit(runnable(period = 1) { throw failure })

        assertSame(failure, assertThrows(IllegalStateException::class.java) { scheduler.action.run() })
        stop(executor)

        assertEquals(listOf(failure), failures)
        assertEquals(0, scheduler.cancelCount)
    }

    @Test
    fun `periodic async failure does not stop scheduler trigger`() {
        val scheduler = RecordingScheduler()
        val async = RecordingExecutorService()
        val failures = ArrayList<Throwable>()
        val executor = executor(scheduler, async, failures)
        val failure = IllegalStateException("boom")
        executor.start()
        executor.submit(runnable(async = true, period = 1) { throw failure })

        scheduler.action.run()
        scheduler.action.run()

        assertEquals(2, async.queuedTaskCount)
        repeat(2) {
            assertSame(failure, assertThrows(IllegalStateException::class.java) { async.runNext() })
        }
        assertEquals(listOf(failure, failure), failures)
    }

    @Test
    fun `task cancellation reaches scheduled future once`() {
        val scheduler = RecordingScheduler()
        val executor = executor(scheduler)
        executor.start()

        val task = executor.submit(runnable(delay = 2, period = 3))
        task.cancel()
        task.cancel()

        assertEquals(1, scheduler.cancelCount)
        assertEquals(2, scheduler.runnable.delay)
        assertEquals(3, scheduler.runnable.period)
    }

    @Test
    fun `stop cancels active tasks shuts down executor and rejects submissions`() {
        val scheduler = RecordingScheduler()
        val async = RecordingExecutorService()
        val executor = executor(scheduler, async)
        executor.start()
        executor.submit(runnable(delay = 1))

        stop(executor)

        assertEquals(1, scheduler.cancelCount)
        assertTrue(async.isShutdown)
        assertThrows(RejectedExecutionException::class.java) {
            executor.submit(runnable())
        }
    }

    @Test
    fun `public constructor and scheduled task field remain available`() {
        HytaleExecutor::class.java.getConstructor()
        assertEquals(ScheduledFuture::class.java, HytaleExecutor.HytaleRunningTask::class.java.getField("scheduledTask").type)
    }

    private fun executor(
        scheduler: RecordingScheduler,
        async: RecordingExecutorService = RecordingExecutorService(),
        failures: MutableList<Throwable> = ArrayList(),
    ): HytaleExecutor {
        val schedulerType = Class.forName("taboolib.platform.HytaleTaskScheduler")
        val schedulerProxy = Proxy.newProxyInstance(
            schedulerType.classLoader,
            arrayOf(schedulerType),
        ) { proxy, method, args ->
            when (method.name) {
                "schedule" -> {
                    val callArgs = requireNotNull(args)
                    scheduler.schedule(
                        callArgs[0] as PlatformExecutor.PlatformRunnable,
                        callArgs[1] as Runnable,
                    )
                }
                "toString" -> "RecordingSchedulerProxy"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> null
            }
        }
        val constructor = HytaleExecutor::class.java.getDeclaredConstructor(
            schedulerType,
            ExecutorService::class.java,
            Class.forName("kotlin.jvm.functions.Function1"),
            java.lang.Boolean.TYPE,
        )
        constructor.isAccessible = true
        val reporter: (Throwable) -> Unit = { failures.add(it) }
        return constructor.newInstance(schedulerProxy, async, reporter, false)
    }

    private fun stop(executor: HytaleExecutor) {
        val method = HytaleExecutor::class.java.getDeclaredMethod("stop")
        method.isAccessible = true
        try {
            method.invoke(executor)
        } catch (ex: InvocationTargetException) {
            throw ex.cause ?: ex
        }
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

    private class RecordingScheduler {

        var scheduleCount = 0
        var cancelCount = 0
        lateinit var action: Runnable
        lateinit var runnable: PlatformExecutor.PlatformRunnable

        fun schedule(runnable: PlatformExecutor.PlatformRunnable, action: Runnable): ScheduledFuture<*> {
            scheduleCount++
            this.runnable = runnable
            this.action = action
            return Proxy.newProxyInstance(
                ScheduledFuture::class.java.classLoader,
                arrayOf(ScheduledFuture::class.java),
            ) { proxy, method, args ->
                when (method.name) {
                    "cancel" -> {
                        cancelCount++
                        true
                    }
                    "isCancelled", "isDone" -> false
                    "toString" -> "RecordedScheduledFuture"
                    "hashCode" -> System.identityHashCode(proxy)
                    "equals" -> proxy === args?.firstOrNull()
                    else -> 0
                }
            } as ScheduledFuture<*>
        }
    }

    private class RecordingExecutorService : AbstractExecutorService() {

        private val tasks = ArrayDeque<Runnable>()
        private var stopped = false

        val queuedTaskCount: Int
            get() = tasks.size

        override fun execute(command: Runnable) {
            if (stopped) {
                throw RejectedExecutionException("executor stopped")
            }
            tasks += command
        }

        fun runNext() {
            tasks.removeFirst().run()
        }

        override fun shutdown() {
            stopped = true
        }

        override fun shutdownNow(): MutableList<Runnable> {
            stopped = true
            return ArrayList(tasks).also { tasks.clear() }
        }

        override fun isShutdown(): Boolean {
            return stopped
        }

        override fun isTerminated(): Boolean {
            return stopped && tasks.isEmpty()
        }

        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
            return isTerminated
        }
    }
}
