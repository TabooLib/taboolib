package taboolib.platform

import com.velocitypowered.api.scheduler.ScheduledTask
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.util.unsafeLazy
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.text.repeat

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
class VelocityExecutor : PlatformExecutor {

    private val tasks = ArrayList<VelocityRunningTask>()
    private var started = false
    private val executor = Executors.newFixedThreadPool(16)

    val plugin by unsafeLazy {
        VelocityPlugin.getInstance()
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

    fun execute(velocityRunningTask: VelocityRunningTask, runnable: PlatformExecutor.PlatformRunnable): ScheduledTask {

        return when {
            runnable.period > 0 -> plugin.server.scheduler
                .buildTask(plugin) {
                    if (runnable.async) {
                        executor.submit { runnable.executor(velocityRunningTask.platformTask()) }
                    } else {
                        runnable.executor(velocityRunningTask.platformTask())
                    }
                }
                .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                .repeat(runnable.period * 50, TimeUnit.MILLISECONDS)
                .schedule()

            runnable.delay > 0 -> plugin.server.scheduler
                .buildTask(plugin) {
                    if (runnable.async) {
                        executor.submit { runnable.executor(velocityRunningTask.platformTask()) }
                    } else {
                        runnable.executor(velocityRunningTask.platformTask())
                    }
                }
                .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                .schedule()

            else -> plugin.server.scheduler
                .buildTask(plugin) {
                    if (runnable.async) {
                        executor.submit { runnable.executor(velocityRunningTask.platformTask()) }
                    } else {
                        runnable.executor(velocityRunningTask.platformTask())
                    }
                }.schedule()
        }
    }

    class VelocityRunningTask(val executor: VelocityExecutor, val runnable: PlatformExecutor.PlatformRunnable) {

        lateinit var scheduledTask: ScheduledTask

        fun executeNow() {
            runnable.executor(VelocityPlatformTask { })
        }

        fun execute() {
            scheduledTask = executor.execute(this, runnable)
        }

        fun platformTask(): PlatformExecutor.PlatformTask {
            return VelocityPlatformTask { scheduledTask.cancel() }
        }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {

        val task = VelocityRunningTask(this, runnable)

        return if (started) {
            if (runnable.now) {
                task.executeNow()
                VelocityPlatformTask { }
            } else {
                task.execute()
                task.platformTask()
            }
        } else {
            tasks += task
            VelocityPlatformTask {
                if (!runnable.now) {
                    task.platformTask().cancel()
                }
                tasks -= task
            }
        }
    }

    class VelocityPlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.close()
        }
    }
}