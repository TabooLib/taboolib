package taboolib.platform

import com.hypixel.hytale.server.core.HytaleServer
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.util.unsafeLazy
import java.io.Closeable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * TabooLib
 * taboolib.platform.HytaleExecutor
 *
 * @author sky
 * @since 2024/1/1
 */
@Awake
@Inject
@PlatformSide(Platform.HYTALE)
class HytaleExecutor private constructor(
    private val taskScheduler: HytaleTaskScheduler?,
    private val asyncExecutor: ExecutorService,
    private val exceptionReporter: (Throwable) -> Unit,
    registerStopTask: Boolean,
) : PlatformExecutor {

    constructor() : this(null, createAsyncExecutor(), ::reportTaskException, true)

    private enum class State {
        NEW, RUNNING, STOPPED
    }

    private val lock = Any()
    private val pendingTasks = LinkedHashSet<HytaleRunningTask>()
    private val activeTasks = LinkedHashSet<HytaleRunningTask>()

    @Volatile
    private var state = State.NEW

    val plugin by unsafeLazy {
        HytalePlugin.getInstance()
    }

    init {
        if (registerStopTask) {
            registerLifeCycleTask(LifeCycle.DISABLE, 2) { stop() }
        }
    }

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        val tasks = synchronized(lock) {
            when (state) {
                State.NEW -> {
                    state = State.RUNNING
                    pendingTasks.filterNotTo(ArrayList()) { it.isCancelled }.also {
                        pendingTasks.clear()
                        activeTasks.addAll(it)
                    }
                }
                State.RUNNING, State.STOPPED -> return
            }
        }
        var failure: Throwable? = null
        tasks.forEach {
            try {
                launch(it)
            } catch (ex: Throwable) {
                if (failure == null) {
                    failure = ex
                } else {
                    failure?.addSuppressed(ex)
                }
            }
        }
        failure?.let { throw it }
    }

    private fun stop() {
        val tasks = synchronized(lock) {
            if (state == State.STOPPED) {
                return
            }
            state = State.STOPPED
            LinkedHashSet<HytaleRunningTask>().also {
                it.addAll(pendingTasks)
                it.addAll(activeTasks)
                pendingTasks.clear()
                activeTasks.clear()
            }
        }
        var failure: Throwable? = null
        tasks.forEach {
            try {
                it.platformTask().cancel()
            } catch (ex: Throwable) {
                if (failure == null) {
                    failure = ex
                } else {
                    failure?.addSuppressed(ex)
                }
            }
        }
        try {
            asyncExecutor.shutdownNow()
        } catch (ex: Throwable) {
            if (failure == null) {
                failure = ex
            } else {
                failure?.addSuppressed(ex)
            }
        }
        failure?.let { throw it }
    }

    fun execute(hytaleRunningTask: HytaleRunningTask, runnable: PlatformExecutor.PlatformRunnable): ScheduledFuture<*> {
        val action = Runnable { executeScheduled(hytaleRunningTask, runnable) }
        taskScheduler?.let { return it.schedule(runnable, action) }
        return HytaleServerTaskScheduler.schedule(runnable, action)
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        val task = HytaleRunningTask(this, runnable)
        val launchNow = synchronized(lock) {
            when (state) {
                State.NEW -> {
                    pendingTasks += task
                    false
                }
                State.RUNNING -> {
                    activeTasks += task
                    true
                }
                State.STOPPED -> throw RejectedExecutionException("HytaleExecutor has been stopped")
            }
        }
        if (launchNow) {
            launch(task)
        }
        return task.platformTask()
    }

    private fun launch(task: HytaleRunningTask) {
        if (task.isCancelled) {
            taskFinished(task)
            return
        }
        if (task.runnable.now) {
            try {
                task.executeNow()
            } finally {
                taskFinished(task)
            }
        } else {
            try {
                task.execute()
            } catch (ex: Throwable) {
                taskFinished(task)
                throw ex
            }
        }
    }

    private fun executeScheduled(task: HytaleRunningTask, runnable: PlatformExecutor.PlatformRunnable) {
        if (task.isCancelled) {
            return
        }
        if (runnable.async) {
            val started = AtomicBoolean(false)
            try {
                asyncExecutor.execute {
                    started.set(true)
                    executeUserTask(task, runnable)
                }
            } catch (ex: Throwable) {
                if (!started.get()) {
                    reportTaskFailure(ex)
                    try {
                        task.platformTask().cancel()
                    } catch (cancellationFailure: Throwable) {
                        ex.addSuppressed(cancellationFailure)
                    }
                }
                throw ex
            }
        } else {
            executeUserTask(task, runnable)
        }
    }

    private fun executeUserTask(task: HytaleRunningTask, runnable: PlatformExecutor.PlatformRunnable) {
        if (task.isCancelled) {
            return
        }
        try {
            runnable.executor(task.platformTask())
        } catch (ex: Throwable) {
            reportTaskFailure(ex)
            if (!runnable.async) {
                taskFinished(task)
            }
            throw ex
        } finally {
            if (runnable.period <= 0) {
                taskFinished(task)
            }
        }
    }

    private fun reportTaskFailure(ex: Throwable) {
        try {
            exceptionReporter(ex)
        } catch (reportingFailure: Throwable) {
            ex.addSuppressed(reportingFailure)
        }
    }

    private fun taskFinished(task: HytaleRunningTask) {
        synchronized(lock) {
            pendingTasks -= task
            activeTasks -= task
        }
    }

    private fun taskCancelled(task: HytaleRunningTask) {
        taskFinished(task)
    }

    class HytaleRunningTask(val executor: HytaleExecutor, val runnable: PlatformExecutor.PlatformRunnable) {

        lateinit var scheduledTask: ScheduledFuture<*>

        private val cancelled = AtomicBoolean(false)
        private val scheduledTaskReference = AtomicReference<ScheduledFuture<*>?>()
        private val scheduledTaskCancelled = AtomicBoolean(false)

        @get:JvmSynthetic
        internal val isCancelled: Boolean
            get() = cancelled.get()

        fun executeNow() {
            if (!isCancelled) {
                executor.executeUserTask(this, runnable)
            }
        }

        fun execute() {
            if (isCancelled) {
                return
            }
            val task = executor.execute(this, runnable)
            scheduledTask = task
            bind(task)
        }

        fun platformTask(): PlatformExecutor.PlatformTask {
            return HytalePlatformTask { cancel() }
        }

        private fun cancel() {
            if (cancelled.compareAndSet(false, true)) {
                try {
                    val scheduled = scheduledTaskReference.get()
                        ?: if (this::scheduledTask.isInitialized) scheduledTask else null
                    scheduled?.let(::cancelScheduledTask)
                } finally {
                    executor.taskCancelled(this)
                }
            }
        }

        private fun bind(task: ScheduledFuture<*>) {
            check(scheduledTaskReference.compareAndSet(null, task)) { "Scheduled task is already bound" }
            if (isCancelled) {
                cancelScheduledTask(task)
            }
        }

        private fun cancelScheduledTask(task: ScheduledFuture<*>) {
            if (scheduledTaskCancelled.compareAndSet(false, true)) {
                task.cancel(false)
            }
        }
    }

    class HytalePlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

        private val cancelled = AtomicBoolean(false)

        override fun cancel() {
            if (cancelled.compareAndSet(false, true)) {
                runnable.close()
            }
        }
    }

    companion object {

        private fun createAsyncExecutor(): ExecutorService {
            return Executors.newFixedThreadPool(16, HytaleAsyncThreadFactory())
        }

        private fun reportTaskException(ex: Throwable) {
            try {
                HytalePlugin.getInstance().logger.atSevere().withCause(ex)
                    .log("Unhandled exception in a TabooLib Hytale task")
            } catch (_: Throwable) {
                PrimitiveIO.error("Unhandled exception in a TabooLib Hytale task: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}

private fun interface HytaleTaskScheduler {

    fun schedule(runnable: PlatformExecutor.PlatformRunnable, action: Runnable): ScheduledFuture<*>
}

private object HytaleServerTaskScheduler : HytaleTaskScheduler {

    override fun schedule(runnable: PlatformExecutor.PlatformRunnable, action: Runnable): ScheduledFuture<*> {
        return when {
            runnable.period > 0 -> HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                action,
                runnable.delay * 50,
                runnable.period * 50,
                TimeUnit.MILLISECONDS
            )
            else -> HytaleServer.SCHEDULED_EXECUTOR.schedule(
                action,
                runnable.delay * 50,
                TimeUnit.MILLISECONDS
            )
        }
    }
}

private class HytaleAsyncThreadFactory : ThreadFactory {

    private val counter = AtomicInteger()

    override fun newThread(runnable: Runnable): Thread {
        return Thread(runnable, "TabooLib-Hytale-Async-${counter.incrementAndGet()}")
    }
}
