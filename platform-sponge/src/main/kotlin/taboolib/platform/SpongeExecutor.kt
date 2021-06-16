package taboolib.platform

import org.spongepowered.api.scheduler.Task
import taboolib.common.platform.PlatformExecutor
import taboolib.common.platform.Awake
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
class SpongeExecutor : PlatformExecutor {

    val plugin = SpongePlugin.instance

    override fun execute(async: Boolean, delay: Long, period: Long, executor: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformTask {
        val future = CompletableFuture<Unit>()
        var task = SpongePlatformTask(future)
        val scheduledTask = when {
            period > 0 -> if (async) {
                Task.builder()
                    .async()
                    .delay(delay * 50, TimeUnit.MILLISECONDS)
                    .interval(period * 50, TimeUnit.MILLISECONDS)
                    .execute(Runnable {
                        task = SpongePlatformTask(future)
                    })
            } else {
                Task.builder()
                    .delay(delay * 50, TimeUnit.MILLISECONDS)
                    .interval(period * 50, TimeUnit.MILLISECONDS)
                    .execute(Runnable {
                        task = SpongePlatformTask(future)
                    })
            }
            delay > 0 -> if (async) {
                Task.builder()
                    .async()
                    .delay(delay * 50, TimeUnit.MILLISECONDS)
                    .execute(Runnable {
                        task = SpongePlatformTask(future)
                    })
            } else {
                Task.builder()
                    .delay(delay * 50, TimeUnit.MILLISECONDS)
                    .execute(Runnable {
                        task = SpongePlatformTask(future)
                    })
            }
            else -> if (async) {
                Task.builder()
                    .async()
                    .execute(Runnable {
                        task = SpongePlatformTask(future)
                    })
            } else {
                Task.builder()
                    .execute(Runnable {
                        task = SpongePlatformTask(future)
                    })
            }
        }
        future.thenAccept {
            scheduledTask.submit(SpongePlugin.instance).cancel()
        }
        return task
    }

    class SpongePlatformTask(private val future: CompletableFuture<Unit>) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            future.complete(null)
        }
    }
}