package taboolib.platform

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformExecutorSupport
import taboolib.common.platform.service.PlatformThreadFactory
import taboolib.common.platform.service.runReportingFailure
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * TabooLib
 * taboolib.platform.AppExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:43
 */
@Awake
@Inject
@PlatformSide(Platform.APPLICATION)
class AppExecutor private constructor(
    private val executor: ScheduledExecutorService,
    private val exceptionReporter: (Throwable) -> Unit,
    registerStopTask: Boolean,
) : PlatformExecutorSupport<AppExecutor.AppPlatformTask>("AppExecutor"), PlatformExecutor {

    constructor() : this(createExecutor(), ::reportTaskException, true)

    init {
        if (registerStopTask) {
            registerStopTaskOnDisable()
        }
    }

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        startTasks()
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        rejectIfStopped()
        val task = AppPlatformTask()
        if (runnable.now) {
            executeUserTask(task, runnable)
            return task
        }
        val command = Runnable {
            if (!task.isCancelled) {
                executeUserTask(task, runnable)
            }
        }
        val future = when {
            runnable.period > 0 -> executor.scheduleAtFixedRate(command, runnable.delay * 50L, runnable.period * 50L, TimeUnit.MILLISECONDS)
            runnable.delay > 0 -> executor.schedule(command, runnable.delay * 50L, TimeUnit.MILLISECONDS)
            else -> executor.schedule(command, 0L, TimeUnit.MILLISECONDS)
        }
        task.attach(future)
        return task
    }

    fun stop() {
        stopTasks()
    }

    /** 关闭调度线程池，已提交的任务由线程池自身负责中断 */
    override fun onStopped() {
        executor.shutdownNow()
    }

    private fun executeUserTask(task: AppPlatformTask, runnable: PlatformExecutor.PlatformRunnable) {
        runReportingFailure(exceptionReporter) { runnable.executor(task) }
    }

    class AppPlatformTask() : PlatformExecutor.PlatformTask {

        private val cancelled = AtomicBoolean(false)
        private val future = AtomicReference<Future<*>?>()
        private var cancellationSignal: CompletableFuture<Unit>? = null

        constructor(cancellationSignal: CompletableFuture<Unit>) : this() {
            this.cancellationSignal = cancellationSignal
        }

        internal val isCancelled: Boolean
            get() = cancelled.get()

        internal fun attach(scheduled: Future<*>) {
            check(future.compareAndSet(null, scheduled)) { "Scheduled task is already bound" }
            if (cancelled.get()) {
                scheduled.cancel(false)
            }
        }

        override fun cancel() {
            if (cancelled.compareAndSet(false, true)) {
                cancellationSignal?.complete(null)
                future.get()?.cancel(false)
            }
        }
    }

    companion object {

        private fun createExecutor(): ScheduledExecutorService {
            return Executors.newScheduledThreadPool(16, AppExecutorThreadFactory())
        }

        private fun reportTaskException(ex: Throwable) {
            PrimitiveIO.error("Application 平台任务执行异常：{0}", ex.message ?: ex.javaClass.name)
            ex.printStackTrace()
        }
    }
}

/** Application 平台线程工厂，线程名形如 TabooLib-Application-Executor-1 */
internal class AppExecutorThreadFactory : ThreadFactory by PlatformThreadFactory("TabooLib-Application-Executor-")
