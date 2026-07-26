package taboolib.platform

import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.scheduler.ScheduledTask
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.CloseablePlatformTask
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformExecutorSupport
import taboolib.common.platform.service.PlatformTaskCancellation
import taboolib.common.platform.service.PlatformTaskRegistration
import taboolib.common.platform.service.runReportingFailure
import java.io.Closeable
import java.util.concurrent.TimeUnit

/**
 * TabooLib
 * taboolib.platform.AfyBrokerExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:43
 */
@Awake
@Inject
@PlatformSide(Platform.AFYBROKER)
class AfyBrokerExecutor : PlatformExecutorSupport<AfyBrokerExecutor.AfyBrokerRunningTask>("AfyBrokerExecutor"), PlatformExecutor {

    init {
        registerStopTaskOnDisable()
    }

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        startTasks()
    }

    fun stop() {
        stopTasks()
    }

    override fun launchTask(task: AfyBrokerRunningTask) {
        if (task.runnable.now) {
            task.execute()
        } else {
            task.execute(task.runnable.async, task.runnable.delay, task.runnable.period)
        }
    }

    override fun cancelTask(task: AfyBrokerRunningTask) {
        task.cancel()
    }

    class AfyBrokerRunningTask(val runnable: PlatformExecutor.PlatformRunnable) {

        private val cancellation = PlatformTaskCancellation<ScheduledTask> { it.cancel() }
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
                        runReportingFailure(::reportTaskFailure, onCompleted) {
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
                        runReportingFailure(::reportTaskFailure, ::cancel) {
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
                    runReportingFailure(::reportTaskFailure) {
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
            onCancelled = { taskFinished(task) },
            onCompleted = { taskFinished(task) }
        )
        val platformTask = task.platformTask()
        when (registerTask(task)) {
            PlatformTaskRegistration.PENDING -> Unit
            PlatformTaskRegistration.ACTIVE -> launchTask(task)
            PlatformTaskRegistration.REJECTED -> {
                task.cancel()
                rejectStopped()
            }
        }
        return platformTask
    }

    class BrokerPlatformTask(runnable: Closeable) : CloseablePlatformTask(runnable)
}
