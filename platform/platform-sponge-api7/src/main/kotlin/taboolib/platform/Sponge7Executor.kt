package taboolib.platform

import org.spongepowered.api.scheduler.Task
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import java.util.concurrent.CompletableFuture
import taboolib.common.util.unsafeLazy

/**
 * TabooLib
 * taboolib.platform.SpongeExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:43
 */
@Awake
@PlatformSide([Platform.SPONGE_API_7])
class Sponge7Executor : PlatformExecutor {

    private val tasks = ArrayList<PlatformExecutor.PlatformRunnable>()
    private var started = false

    val plugin by unsafeLazy {
        Sponge7Plugin.getInstance()
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
                        .delayTicks(runnable.delay)
                        .intervalTicks(runnable.period)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin.pluginContainer)
                } else {
                    Task.builder()
                        .delayTicks(runnable.delay)
                        .intervalTicks(runnable.period)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin.pluginContainer)
                }
                runnable.delay > 0 -> if (runnable.async) {
                    Task.builder()
                        .async()
                        .delayTicks(runnable.delay)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin)
                } else {
                    Task.builder()
                        .delayTicks(runnable.delay)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin)
                }
                else -> if (runnable.async) {
                    Task.builder()
                        .async()
                        .execute(Runnable {
                            runnable.executor(task)
                        }).submit(plugin)
                } else {
                    Task.builder()
                        .execute(Runnable {
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