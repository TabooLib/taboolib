package taboolib.platform

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.TabooLib
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
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
) : PlatformExecutor {

    constructor() : this(createExecutor(), ::reportTaskException, true)

    internal enum class State {
        NEW, RUNNING, STOPPED
    }

    private val state = AtomicReference(State.NEW)

    init {
        if (registerStopTask) {
            TabooLib.registerLifeCycleTask(LifeCycle.DISABLE, 2) { stop() }
        }
    }

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        state.compareAndSet(State.NEW, State.RUNNING)
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
        if (state.getAndSet(State.STOPPED) != State.STOPPED) {
            executor.shutdownNow()
        }
    }

    internal fun currentState(): State = state.get()

    private fun rejectIfStopped() {
        if (state.get() == State.STOPPED) {
            throw RejectedExecutionException("AppExecutor has been stopped")
        }
    }

    private fun executeUserTask(task: AppPlatformTask, runnable: PlatformExecutor.PlatformRunnable) {
        runAppTask(exceptionReporter) { runnable.executor(task) }
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

internal class AppExecutorThreadFactory : ThreadFactory {

    private val counter = AtomicInteger()

    override fun newThread(runnable: Runnable): Thread {
        return Thread(runnable, "TabooLib-Application-Executor-${counter.incrementAndGet()}")
    }
}

internal inline fun <T> runAppTask(reporter: (Throwable) -> Unit, action: () -> T): T {
    try {
        return action()
    } catch (ex: Throwable) {
        try {
            reporter(ex)
        } catch (reportingFailure: Throwable) {
            ex.addSuppressed(reportingFailure)
        }
        throw ex
    }
}
