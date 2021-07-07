package taboolib.platform

import org.spongepowered.api.scheduler.Task
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformExecutor
import taboolib.common.platform.PlatformSide
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * TabooLib
 * taboolib.platform.SpongeExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:43
 */
@Awake
@PlatformSide([Platform.SPONGE_API_7])
class SpongeExecutor : PlatformExecutor {

    private val tasks = ArrayList<PlatformExecutor.PlatformRunnable>()
    private var started = false

    val plugin by lazy {
        SpongePlugin.getInstance()
    }

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        if (started) {
            val future = CompletableFuture<Unit>()
            val task = SpongePlatformTask(future)
            val scheduledTask = when {
                runnable.now -> {
                    runnable.executor(task)
                    null
                }
                runnable.period > 0 -> if (runnable.async) {
                    Task.builder()
                        .async()
                        .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                        .interval(runnable.period * 50, TimeUnit.MILLISECONDS)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin)
                } else {
                    Task.builder()
                        .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                        .interval(runnable.period * 50, TimeUnit.MILLISECONDS)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin)
                }
                runnable.delay > 0 -> if (runnable.async) {
                    Task.builder()
                        .async()
                        .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin)
                } else {
                    Task.builder()
                        .delay(runnable.delay * 50, TimeUnit.MILLISECONDS)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin)
                }
                else -> if (runnable.async) {
                    Task.builder().async().execute(Runnable {
                        runnable.executor(task)
                    }).submit(plugin)
                } else {
                    Task.builder().execute(Runnable {
                        runnable.executor(task)
                    }).submit(plugin)
                }
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

    class SpongePlatformTask(private val future: CompletableFuture<Unit>) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            future.complete(null)
        }
    }
}