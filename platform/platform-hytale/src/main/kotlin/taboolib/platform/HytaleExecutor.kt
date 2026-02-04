package taboolib.platform

import com.hypixel.hytale.server.core.HytaleServer
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.util.unsafeLazy
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

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
class HytaleExecutor : PlatformExecutor {

    private val tasks = ArrayList<HytaleRunningTask>()
    private var started = false
    private val executor = Executors.newFixedThreadPool(16)

    val plugin by unsafeLazy {
        HytalePlugin.getInstance()
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

    fun execute(hytaleRunningTask: HytaleRunningTask, runnable: PlatformExecutor.PlatformRunnable): ScheduledFuture<*> {
        return when {
            runnable.period > 0 -> HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                {
                    if (runnable.async) {
                        executor.submit { runnable.executor(hytaleRunningTask.platformTask()) }
                    } else {
                        runnable.executor(hytaleRunningTask.platformTask())
                    }
                },
                runnable.delay * 50,
                runnable.period * 50,
                TimeUnit.MILLISECONDS
            )

            runnable.delay > 0 -> HytaleServer.SCHEDULED_EXECUTOR.schedule(
                {
                    if (runnable.async) {
                        executor.submit { runnable.executor(hytaleRunningTask.platformTask()) }
                    } else {
                        runnable.executor(hytaleRunningTask.platformTask())
                    }
                },
                runnable.delay * 50,
                TimeUnit.MILLISECONDS
            )

            else -> HytaleServer.SCHEDULED_EXECUTOR.schedule(
                {
                    if (runnable.async) {
                        executor.submit { runnable.executor(hytaleRunningTask.platformTask()) }
                    } else {
                        runnable.executor(hytaleRunningTask.platformTask())
                    }
                },
                0,
                TimeUnit.MILLISECONDS
            )
        }
    }

    class HytaleRunningTask(val executor: HytaleExecutor, val runnable: PlatformExecutor.PlatformRunnable) {

        lateinit var scheduledTask: ScheduledFuture<*>

        fun executeNow() {
            runnable.executor(HytalePlatformTask { })
        }

        fun execute() {
            scheduledTask = executor.execute(this, runnable)
        }

        fun platformTask(): PlatformExecutor.PlatformTask {
            return HytalePlatformTask { scheduledTask.cancel(false) }
        }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        val task = HytaleRunningTask(this, runnable)

        return if (started) {
            if (runnable.now) {
                task.executeNow()
                HytalePlatformTask { }
            } else {
                task.execute()
                task.platformTask()
            }
        } else {
            tasks += task
            HytalePlatformTask {
                if (!runnable.now) {
                    task.platformTask().cancel()
                }
                tasks -= task
            }
        }
    }

    class HytalePlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.close()
        }
    }
}
