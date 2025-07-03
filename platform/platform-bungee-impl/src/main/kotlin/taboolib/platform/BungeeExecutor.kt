package taboolib.platform

import net.md_5.bungee.api.scheduler.ScheduledTask
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.util.unsafeLazy
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * TabooLib
 * taboolib.platform.BungeeExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:13
 */
@Awake
@Inject
@PlatformSide(Platform.BUNGEE)
class BungeeExecutor : PlatformExecutor {

    private val tasks = ArrayList<BungeeRunningTask>()
    private var started = false

    val plugin by unsafeLazy {
        BungeePlugin.getInstance()
    }

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        started = true
        tasks.forEach {
            if (it.runnable.now) {
                it.executeNow()
            } else {
                it.execute()
            }
        }
        tasks.clear()
    }
    fun execute(bungeeRunningTask: BungeeRunningTask, runnable: PlatformExecutor.PlatformRunnable): ScheduledTask {
        val scheduler = plugin.proxy.scheduler
        return when {
            runnable.period > 0 -> if (runnable.async) {
                scheduler.schedule(plugin, {
                    scheduler.runAsync(plugin) {
                        runnable.executor(bungeeRunningTask.platformTask())
                    }
                }, runnable.delay * 50, runnable.period * 50, TimeUnit.MILLISECONDS)
            } else {
                scheduler.schedule(plugin, {
                    runnable.executor(bungeeRunningTask.platformTask())
                }, runnable.delay * 50, runnable.period * 50, TimeUnit.MILLISECONDS)
            }

            runnable.delay > 0 -> if (runnable.async) {
                scheduler.schedule(plugin, {
                    scheduler.runAsync(plugin) {
                        runnable.executor(bungeeRunningTask.platformTask())
                    }
                }, runnable.delay * 50, 0, TimeUnit.MILLISECONDS)
            } else {
                scheduler.schedule(plugin, {
                    runnable.executor(bungeeRunningTask.platformTask())
                }, runnable.delay * 50, 0, TimeUnit.MILLISECONDS)
            }

            else -> if (runnable.async) {
                scheduler.runAsync(plugin) {
                    runnable.executor(bungeeRunningTask.platformTask())
                }
            } else {
                scheduler.schedule(plugin, {
                    runnable.executor(bungeeRunningTask.platformTask())
                }, 0, 0, TimeUnit.MILLISECONDS)
            }
        }
    }

    class BungeeRunningTask(val executor: BungeeExecutor, val runnable: PlatformExecutor.PlatformRunnable) {

        lateinit var scheduledTask: ScheduledTask

        fun executeNow() {
            runnable.executor(BungeePlatformTask { })
        }

        fun execute() {
            scheduledTask = executor.execute(this, runnable)
        }

        fun platformTask(): PlatformExecutor.PlatformTask {
            return BungeePlatformTask { scheduledTask.cancel() }
        }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {

        val task = BungeeRunningTask(this, runnable)

        return if (started) {
            if (runnable.now) {
                task.executeNow()
                BungeePlatformTask { }
            } else {
                task.execute()
                task.platformTask()
            }
        } else {
            tasks += task
            BungeePlatformTask {
                if (!runnable.now) {
                    task.platformTask().cancel()
                }
                tasks -= task
            }
        }
    }

    class BungeePlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.close()
        }
    }
}