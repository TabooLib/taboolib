package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.scheduler.Task
import org.spongepowered.api.util.Ticks
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
@PlatformSide([Platform.SPONGE_API_8])
class Sponge8Executor : PlatformExecutor {

    private val tasks = ArrayList<PlatformExecutor.PlatformRunnable>()
    private var started = false

    val plugin by unsafeLazy {
        Sponge8Plugin.getInstance()
    }

    private val schedulerSync by unsafeLazy {
        Sponge.server().scheduler()
    }

    private val schedulerAsync by unsafeLazy {
        Sponge.game().asyncScheduler()
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
                    schedulerAsync.submit(Task.builder()
                        .plugin(plugin.pluginContainer)
                        .delay(Ticks.of(runnable.delay))
                        .interval(Ticks.of(runnable.period))
                        .execute(Runnable {
                            runnable.executor(task)
                        }).build())
                } else {
                    schedulerSync.submit(Task.builder()
                        .plugin(plugin.pluginContainer)
                        .delay(Ticks.of(runnable.delay))
                        .interval(Ticks.of(runnable.period))
                        .execute(Runnable {
                            runnable.executor(task)
                        }).build())
                }
                runnable.delay > 0 -> if (runnable.async) {
                    schedulerAsync.submit(Task.builder()
                        .plugin(plugin.pluginContainer)
                        .delay(Ticks.of(runnable.delay))
                        .execute(Runnable {
                            runnable.executor(task)
                        }).build())
                } else {
                    schedulerSync.submit(Task.builder()
                        .plugin(plugin.pluginContainer)
                        .delay(Ticks.of(runnable.delay))
                        .execute(Runnable {
                            runnable.executor(task)
                        }).build())
                }
                else -> if (runnable.async) {
                    schedulerAsync.submit(Task.builder()
                        .plugin(plugin.pluginContainer)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).build())
                } else {
                    schedulerSync.submit(Task.builder()
                        .plugin(plugin.pluginContainer)
                        .execute(Runnable {
                            runnable.executor(task)
                        }).build())
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