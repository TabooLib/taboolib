package taboolib.platform

import taboolib.common.platform.PlatformExecutor
import taboolib.common.platform.PlatformInstance
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * TabooLib
 * taboolib.platform.BungeeExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:13
 */
@PlatformInstance
class BungeeExecutor : PlatformExecutor {

    val plugin = BungeePlugin.instance

    override fun execute(async: Boolean, delay: Long, period: Long, executor: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformTask {
        val scheduler = plugin.proxy.scheduler
        val future = CompletableFuture<Unit>()
        val task: BungeePlatformTask
        val scheduledTask = when {
            period > 0 -> if (async) {
                scheduler.schedule(plugin, object : Runnable {

                    init {
                        task = BungeePlatformTask(future)
                    }

                    override fun run() {
                        scheduler.runAsync(plugin) {
                            executor(task)
                        }
                    }
                }, delay, period * 50, TimeUnit.MILLISECONDS)
            } else {
                scheduler.schedule(plugin, object : Runnable {

                    init {
                        task = BungeePlatformTask(future)
                    }

                    override fun run() {
                        executor(task)
                    }
                }, delay, period * 50, TimeUnit.MILLISECONDS)
            }
            delay > 0 -> if (async) {
                scheduler.schedule(plugin, object : Runnable {

                    init {
                        task = BungeePlatformTask(future)
                    }

                    override fun run() {
                        scheduler.runAsync(plugin) {
                            executor(task)
                        }
                    }
                }, delay, 0, TimeUnit.MILLISECONDS)
            } else {
                scheduler.schedule(plugin, object : Runnable {

                    init {
                        task = BungeePlatformTask(future)
                    }

                    override fun run() {
                        executor(task)
                    }
                }, delay, 0, TimeUnit.MILLISECONDS)
            }
            else -> if (async) {
                scheduler.runAsync(plugin, object : Runnable {

                    init {
                        task = BungeePlatformTask(future)
                    }

                    override fun run() {
                        executor(task)
                    }
                })
            } else {
                scheduler.schedule(plugin, object : Runnable {

                    init {
                        task = BungeePlatformTask(future)
                    }

                    override fun run() {
                        executor(task)
                    }
                }, 0, 0, TimeUnit.MILLISECONDS)
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