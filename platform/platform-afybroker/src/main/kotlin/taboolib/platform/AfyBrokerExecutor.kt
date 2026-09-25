package taboolib.platform

import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.scheduler.ScheduledTask
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.TabooLib
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import java.io.Closeable
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TabooLib
 * taboolib.platform.AppExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:43
 */
@Awake
@Inject
@PlatformSide(Platform.AFYBROKER)
class AfyBrokerExecutor : PlatformExecutor {

    private val tasks = AfyBrokerTaskRegistry<AfyBrokerRunningTask>()

    init {
        TabooLib.registerLifeCycleTask(LifeCycle.DISABLE, 2) { stop() }
    }

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        executeAll(tasks.start())
    }

    fun stop() {
        cancelAll(tasks.stop())
    }

    private fun executeAll(pendingTasks: List<AfyBrokerRunningTask>) {
        var failure: Throwable? = null
        pendingTasks.forEach { task ->
            try {
                execute(task)
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

    private fun cancelAll(activeTasks: List<AfyBrokerRunningTask>) {
        var failure: Throwable? = null
        activeTasks.forEach { task ->
            try {
                task.cancel()
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

    private fun execute(task: AfyBrokerRunningTask) {
        if (task.runnable.now) {
            task.execute()
        } else {
            task.execute(task.runnable.async, task.runnable.delay, task.runnable.period)
        }
    }

    class AfyBrokerRunningTask(val runnable: PlatformExecutor.PlatformRunnable) {

        private val cancellation = AfyBrokerTaskCancellation<ScheduledTask> { it.cancel() }
        private var onCancelled: () -> Unit = {}
        private var onCompleted: () -> Unit = {}

        lateinit var scheduledTask: ScheduledTask

        internal fun observe(onCancelled: () -> Unit, onCompleted: () -> Unit) {
            this.onCancelled = onCancelled
            this.onCompleted = onCompleted
        }

        fun execute() {
            executeUserTask(completeAfterRun = true)
        }

        fun execute(async: Boolean, delay: Long, period: Long) {
            if (cancellation.isCancelled()) {
                onCompleted()
                return
            }
            try {
                val scheduled = if (period < 1) {
                    scheduleOnce(async, delay)
                } else {
                    scheduleRepeated(async, delay, period)
                }
                scheduledTask = scheduled
                cancellation.bind(scheduled)
            } catch (ex: Throwable) {
                onCompleted()
                throw ex
            }
        }

        private fun scheduleOnce(async: Boolean, delay: Long): ScheduledTask {
            return if (async) {
                Broker.getScheduler().schedule(AfyBrokerPlugin.getInstance(), {
                    if (!cancellation.isCancelled()) {
                        runAfyBrokerDispatch(::reportTaskFailure, onCompleted) {
                            Broker.getScheduler().runAsync(AfyBrokerPlugin.getInstance()) {
                                executeUserTask(completeAfterRun = true)
                            }
                        }
                    } else {
                        onCompleted()
                    }
                }, delay * 50L, TimeUnit.MILLISECONDS)
            } else {
                Broker.getScheduler().schedule(AfyBrokerPlugin.getInstance(), {
                    executeUserTask(completeAfterRun = true)
                }, delay * 50L, TimeUnit.MILLISECONDS)
            }
        }

        private fun scheduleRepeated(async: Boolean, delay: Long, period: Long): ScheduledTask {
            return if (async) {
                Broker.getScheduler().schedule(AfyBrokerPlugin.getInstance(), {
                    if (!cancellation.isCancelled()) {
                        runAfyBrokerDispatch(::reportTaskFailure, ::cancel) {
                            Broker.getScheduler().runAsync(AfyBrokerPlugin.getInstance()) {
                                executeUserTask(completeAfterRun = false)
                            }
                        }
                    }
                }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS)
            } else {
                Broker.getScheduler().schedule(AfyBrokerPlugin.getInstance(), {
                    executeUserTask(completeAfterRun = false)
                }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS)
            }
        }

        private fun executeUserTask(completeAfterRun: Boolean) {
            try {
                cancellation.runIfActive {
                    runAfyBrokerTask(::reportTaskFailure) {
                        runnable.executor(platformTask())
                    }
                }
            } finally {
                if (completeAfterRun) {
                    onCompleted()
                }
            }
        }

        fun platformTask(): PlatformExecutor.PlatformTask {
            return BrokerPlatformTask { cancel() }
        }

        internal fun cancel() {
            if (this::scheduledTask.isInitialized) {
                cancellation.bind(scheduledTask)
            }
            cancellation.cancel(onCancelled)
        }

        private fun reportTaskFailure(ex: Throwable) {
            PrimitiveIO.error(
                "AfyBroker 平台任务执行异常：{0}",
                ex.message ?: ex.javaClass.name
            )
            ex.printStackTrace()
        }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        val task = AfyBrokerRunningTask(runnable)
        task.observe(
            onCancelled = { tasks.remove(task) },
            onCompleted = { tasks.remove(task) }
        )
        val platformTask = task.platformTask()
        when (tasks.register(task)) {
            AfyBrokerTaskRegistration.PENDING -> Unit
            AfyBrokerTaskRegistration.ACTIVE -> execute(task)
            AfyBrokerTaskRegistration.REJECTED -> {
                task.cancel()
                throw RejectedExecutionException("AfyBrokerExecutor has been stopped")
            }
        }
        return platformTask
    }

    class BrokerPlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

        private val cancelled = AtomicBoolean()

        override fun cancel() {
            if (cancelled.compareAndSet(false, true)) {
                runnable.close()
            }
        }
    }
}
