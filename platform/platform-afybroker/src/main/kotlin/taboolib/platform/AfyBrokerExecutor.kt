package taboolib.platform

import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.scheduler.ScheduledTask
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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

    private val tasks = ArrayList<AfyBrokerRunningTask>()
    private var started = false

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        started = true
        // 提交列队中的任务
        tasks.forEach {
            if (it.runnable.now) {
                it.execute()
            } else {
                it.execute(it.runnable.async, it.runnable.delay, it.runnable.period)
            }
        }
        tasks.clear()
    }

    class AfyBrokerRunningTask(val runnable: PlatformExecutor.PlatformRunnable) {

        lateinit var scheduledTask: ScheduledTask

        fun execute() {
            runnable.executor(BrokerPlatformTask { })
        }

        fun execute(async: Boolean, delay: Long, period: Long) {
            scheduledTask = if (period < 1) {
                if (async) {
                    Broker.getScheduler().schedule(AfyBrokerPlugin.getInstance(), {
                        Broker.getScheduler().runAsync(AfyBrokerPlugin.getInstance()) {
                            runnable.executor(platformTask())
                        }
                    }, delay * 50L, TimeUnit.MILLISECONDS)
                } else {
                    Broker.getScheduler().schedule(AfyBrokerPlugin.getInstance(), {
                        runnable.executor(platformTask())
                    }, delay * 50L, TimeUnit.MILLISECONDS)
                }
            } else {
                if (async) {
                    Broker.getScheduler().schedule(AfyBrokerPlugin.getInstance(), {
                        Broker.getScheduler().runAsync(AfyBrokerPlugin.getInstance()) {
                            runnable.executor(platformTask())
                        }
                    }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS)
                } else {
                    Broker.getScheduler().schedule(AfyBrokerPlugin.getInstance(), {
                        runnable.executor(platformTask())
                    }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS)
                }
            }
        }

        fun platformTask(): PlatformExecutor.PlatformTask {
            return BrokerPlatformTask { scheduledTask.cancel() }
        }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        val task = AfyBrokerRunningTask(runnable)
        return if (started) {
            if (runnable.now) {
                task.execute()
            } else {
                task.execute(runnable.async, runnable.delay, runnable.period)
            }
            task.platformTask()
        } else {
            tasks += task
            BrokerPlatformTask {
                if (!task.runnable.now) {
                    task.platformTask().cancel()
                }
                tasks -= task
            }
        }
    }

    class BrokerPlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.close()
        }
    }
}