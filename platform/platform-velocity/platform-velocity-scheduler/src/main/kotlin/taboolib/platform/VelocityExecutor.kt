package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformRunnable
import taboolib.common.platform.service.PlatformTask
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * TabooLib
 * taboolib.platform.VelocityExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:43
 */
@Awake
@PlatformSide([Platform.VELOCITY])
class VelocityExecutor : PlatformExecutor {

    private val tasks = ArrayList<PlatformRunnable>()
    private var started = false
    private val executor = Executors.newFixedThreadPool(16)

    val plugin by lazy {
        VelocityPlugin.getInstance()
    }

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformRunnable): PlatformTask {
        if (started) {
            val future = CompletableFuture<Unit>()
            val task = VelocityPlatformTask(future)
            val scheduledTask = when {
                runnable.period > 0 -> plugin.server.scheduler
                    .buildTask(plugin) {
                        if (runnable.async) {
                            executor.submit { runnable.executor(task) }
                        } else {
                            runnable.executor(task)
                        }
                    }
                    .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                    .repeat(runnable.period * 50, TimeUnit.MILLISECONDS)
                    .schedule()
                runnable.delay > 0 -> plugin.server.scheduler
                    .buildTask(plugin) {
                        if (runnable.async) {
                            executor.submit { runnable.executor(task) }
                        } else {
                            runnable.executor(task)
                        }
                    }
                    .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                    .schedule()
                else -> plugin.server.scheduler
                    .buildTask(plugin) {
                        if (runnable.async) {
                            executor.submit { runnable.executor(task) }
                        } else {
                            runnable.executor(task)
                        }
                    }.schedule()
            }
            future.thenAccept {
                scheduledTask?.cancel()
            }
            return task
        } else {
            tasks += runnable
            return object : PlatformTask {

                override fun cancel() {
                    tasks -= runnable
                }
            }
        }
    }
}