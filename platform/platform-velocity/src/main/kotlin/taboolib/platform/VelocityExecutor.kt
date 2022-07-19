package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import taboolib.common.util.unsafeLazy

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

    private val tasks = ArrayList<PlatformExecutor.PlatformRunnable>()
    private var started = false
    private val executor = Executors.newFixedThreadPool(16)

    val plugin by unsafeLazy {
        VelocityPlugin.getInstance()
    }

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        if (started) {
            val future = CompletableFuture<Unit>()
            val task = VelocityPlatformTask(future)
            val scheduledTask = when {
                runnable.now -> {
                    runnable.executor(task)
                    null
                }
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
            return object : PlatformExecutor.PlatformTask {

                override fun cancel() {
                    tasks -= runnable
                }
            }
        }
    }

    class VelocityPlatformTask(private val future: CompletableFuture<Unit>) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            future.complete(null)
        }
    }
}