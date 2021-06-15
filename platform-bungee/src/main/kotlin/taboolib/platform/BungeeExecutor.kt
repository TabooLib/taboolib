package taboolib.platform

import taboolib.common.platform.PlatformInstance
import taboolib.common.platform.PlatformExecutor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@PlatformInstance
class BungeeExecutor : PlatformExecutor {

    override fun execute(async: Boolean, delay: Long, period: Long, executor: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformTask {
        val scheduler = BungeePlugin.instance.proxy.scheduler
        val future = CompletableFuture<Unit>()
        val task: BungeePlatformTask
        val scheduledTask = when {
            period > 0 -> if (async) {
                scheduler.schedule(BungeePlugin.instance, object : Runnable {

                    init {
                        task = BungeePlatformTask(future)
                    }

                    override fun run() {
                        scheduler.runAsync(BungeePlugin.instance) {
                            executor(task)
                        }
                    }
                }, delay, period * 50, TimeUnit.MILLISECONDS)
            } else {
                error(",.")
            }
            delay > 0 -> if (async) {
                error(",.")
            } else {
                error(",.")
            }
            else -> if (async) {
                error(",.")
            } else {
                error(",.")
            }
        }
        future.thenAccept {
            scheduledTask.cancel()
        }
        return task
    }

    class BungeePlatformTask(private val future: CompletableFuture<Unit>) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            future.complete(null)
        }
    }
}