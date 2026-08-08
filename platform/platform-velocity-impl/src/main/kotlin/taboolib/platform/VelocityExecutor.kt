package taboolib.platform

import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.LoggerFactory
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.CloseablePlatformTask
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformExecutorSupport
import taboolib.common.platform.service.PlatformTaskRegistration
import taboolib.common.platform.service.PlatformThreadFactory
import taboolib.common.util.unsafeLazy
import java.io.Closeable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
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
) : PlatformExecutorSupport<VelocityExecutor.VelocityRunningTask>("VelocityExecutor"), PlatformExecutor {

    constructor() : this(null, createAsyncExecutor(), ::reportTaskException, true)

    val plugin by unsafeLazy {
        VelocityPlugin.getInstance()
    }

    init {
        if (registerStopTask) {
            registerStopTaskOnDisable()
        }
    }

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        startTasks()
    }

    fun stop() {
        stopTasks()
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
        when (registerTask(task)) {
            PlatformTaskRegistration.PENDING -> Unit
            PlatformTaskRegistration.ACTIVE -> launchTask(task)
            PlatformTaskRegistration.REJECTED -> rejectStopped()
        }
        return task.platformTask()
    }

    /** 关闭 Velocity 平台的异步线程池 */
    override fun onStopped() {
        asyncExecutor.shutdownNow()
    }

    override fun isTaskCancelled(task: VelocityRunningTask): Boolean = task.isCancelled

    override fun cancelTask(task: VelocityRunningTask) {
        task.cancel()
    }

    override fun launchTask(task: VelocityRunningTask) {
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

    internal fun taskCancelled(task: VelocityRunningTask) {
        taskFinished(task)
    }

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

    class VelocityPlatformTask(runnable: Closeable) : CloseablePlatformTask(runnable)

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

/** Velocity 异步线程工厂，线程名形如 TabooLib-Velocity-Async-1 */
internal class VelocityAsyncThreadFactory : ThreadFactory by PlatformThreadFactory("TabooLib-Velocity-Async-")
