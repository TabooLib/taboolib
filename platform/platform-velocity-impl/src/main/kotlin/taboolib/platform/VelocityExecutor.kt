package taboolib.platform

import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.LoggerFactory
import taboolib.common.Inject
import taboolib.common.LifeCycle
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
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * TabooLib
 * taboolib.platform.VelocityExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:43
 */
@Awake
@Inject
@PlatformSide(Platform.VELOCITY)
class VelocityExecutor internal constructor(
    private val taskScheduler: VelocityTaskScheduler?,
    private val asyncExecutor: ExecutorService,
    private val exceptionReporter: (Throwable) -> Unit,
    registerStopTask: Boolean,
) : PlatformExecutor {

    constructor() : this(null, createAsyncExecutor(), ::reportTaskException, true)

    internal enum class State {
        NEW, RUNNING, STOPPED
    }

    private val lock = Any()
    private val pendingTasks = LinkedHashSet<VelocityRunningTask>()
    private val activeTasks = LinkedHashSet<VelocityRunningTask>()

    @Volatile
    private var state = State.NEW

    val plugin by unsafeLazy {
        VelocityPlugin.getInstance()
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

    fun stop() {
        val tasks = synchronized(lock) {
            if (state == State.STOPPED) {
                return
            }
            state = State.STOPPED
            LinkedHashSet<VelocityRunningTask>().also {
                it.addAll(pendingTasks)
                it.addAll(activeTasks)
                pendingTasks.clear()
                activeTasks.clear()
            }
        }
        var failure: Throwable? = null
        tasks.forEach {
            try {
                it.cancel()
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

    fun execute(velocityRunningTask: VelocityRunningTask, runnable: PlatformExecutor.PlatformRunnable): ScheduledTask {
        val action = Runnable { executeScheduled(velocityRunningTask, runnable) }
        taskScheduler?.let { return it.schedule(velocityRunningTask, runnable, action) }
        return when {
            runnable.period > 0 -> plugin.server.scheduler
                .buildTask(plugin, action)
                .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                .repeat(runnable.period * 50, TimeUnit.MILLISECONDS)
                .schedule()

            runnable.delay > 0 -> plugin.server.scheduler
                .buildTask(plugin, action)
                .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                .schedule()

            else -> plugin.server.scheduler.buildTask(plugin, action).schedule()
        }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        val task = VelocityRunningTask(this, runnable)
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
                State.STOPPED -> throw RejectedExecutionException("VelocityExecutor has been stopped")
            }
        }
        if (launchNow) {
            launch(task)
        }
        return task.platformTask()
    }

    private fun launch(task: VelocityRunningTask) {
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

    private fun executeScheduled(task: VelocityRunningTask, runnable: PlatformExecutor.PlatformRunnable) {
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
                        task.cancel()
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

    private fun executeUserTask(task: VelocityRunningTask, runnable: PlatformExecutor.PlatformRunnable) {
        if (task.isCancelled) {
            return
        }
        try {
            runnable.executor(task.platformTask())
        } catch (ex: Throwable) {
            reportTaskFailure(ex)
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

    private fun taskFinished(task: VelocityRunningTask) {
        synchronized(lock) {
            pendingTasks -= task
            activeTasks -= task
        }
    }

    internal fun taskCancelled(task: VelocityRunningTask) {
        taskFinished(task)
    }

    internal fun currentState(): State = state

    internal fun pendingTaskCount(): Int = synchronized(lock) { pendingTasks.size }

    internal fun activeTaskCount(): Int = synchronized(lock) { activeTasks.size }

    class VelocityRunningTask(val executor: VelocityExecutor, val runnable: PlatformExecutor.PlatformRunnable) {

        lateinit var scheduledTask: ScheduledTask

        private val cancelled = AtomicBoolean(false)
        private val scheduledTaskReference = AtomicReference<ScheduledTask?>()
        private val scheduledTaskCancelled = AtomicBoolean(false)

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
            return VelocityPlatformTask { cancel() }
        }

        fun cancel() {
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

        private fun bind(task: ScheduledTask) {
            check(scheduledTaskReference.compareAndSet(null, task)) { "Scheduled task is already bound" }
            if (isCancelled) {
                cancelScheduledTask(task)
            }
        }

        private fun cancelScheduledTask(task: ScheduledTask) {
            if (scheduledTaskCancelled.compareAndSet(false, true)) {
                task.cancel()
            }
        }
    }

    class VelocityPlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

        private val cancelled = AtomicBoolean(false)

        override fun cancel() {
            if (cancelled.compareAndSet(false, true)) {
                runnable.close()
            }
        }
    }

    companion object {

        private fun createAsyncExecutor(): ExecutorService {
            return Executors.newFixedThreadPool(16, VelocityAsyncThreadFactory())
        }

        private fun reportTaskException(ex: Throwable) {
            val logger = try {
                VelocityPlugin.getInstance().logger
            } catch (_: Throwable) {
                LoggerFactory.getLogger(VelocityExecutor::class.java)
            }
            logger.error("Unhandled exception in a TabooLib Velocity task", ex)
        }
    }
}

internal interface VelocityTaskScheduler {

    fun schedule(task: VelocityExecutor.VelocityRunningTask, runnable: PlatformExecutor.PlatformRunnable, action: Runnable): ScheduledTask
}

internal class VelocityAsyncThreadFactory : ThreadFactory {

    private val counter = AtomicInteger()

    override fun newThread(runnable: Runnable): Thread {
        return Thread(runnable, "TabooLib-Velocity-Async-${counter.incrementAndGet()}")
    }
}
