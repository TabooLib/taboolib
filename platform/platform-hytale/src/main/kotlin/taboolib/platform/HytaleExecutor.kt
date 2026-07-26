package taboolib.platform

import com.hypixel.hytale.server.core.HytaleServer
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
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
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
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
) : PlatformExecutorSupport<HytaleExecutor.HytaleRunningTask>("HytaleExecutor"), PlatformExecutor {

    constructor() : this(null, createAsyncExecutor(), ::reportTaskException, true)

    val plugin by unsafeLazy {
        HytalePlugin.getInstance()
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

    private fun stop() {
        stopTasks()
    }

    fun execute(hytaleRunningTask: HytaleRunningTask, runnable: PlatformExecutor.PlatformRunnable): ScheduledFuture<*> {
        val action = Runnable { executeScheduled(hytaleRunningTask, runnable) }
        taskScheduler?.let { return it.schedule(runnable, action) }
        return HytaleServerTaskScheduler.schedule(runnable, action)
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        val task = HytaleRunningTask(this, runnable)
        when (registerTask(task)) {
            PlatformTaskRegistration.PENDING -> Unit
            PlatformTaskRegistration.ACTIVE -> launchTask(task)
            PlatformTaskRegistration.REJECTED -> rejectStopped()
        }
        return task.platformTask()
    }

    /** 关闭 Hytale 平台的异步线程池 */
    override fun onStopped() {
        asyncExecutor.shutdownNow()
    }

    override fun isTaskCancelled(task: HytaleRunningTask): Boolean = task.isCancelled

    override fun cancelTask(task: HytaleRunningTask) {
        task.platformTask().cancel()
    }

    override fun launchTask(task: HytaleRunningTask) {
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

    class HytalePlatformTask(runnable: Closeable) : CloseablePlatformTask(runnable)

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

/** Hytale 异步线程工厂，线程名形如 TabooLib-Hytale-Async-1 */
private class HytaleAsyncThreadFactory : ThreadFactory by PlatformThreadFactory("TabooLib-Hytale-Async-")
