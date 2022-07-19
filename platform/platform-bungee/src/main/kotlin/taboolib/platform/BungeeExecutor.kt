package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.util.unsafeLazy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * TabooLib
 * taboolib.platform.BungeeExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:13
 */
@Awake
@PlatformSide([Platform.BUNGEE])
class BungeeExecutor : PlatformExecutor {

    private val tasks = ArrayList<PlatformExecutor.PlatformRunnable>()
    private var started = false

    val plugin by unsafeLazy {
        BungeePlugin.getInstance()
    }

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        if (started) {
            val scheduler = plugin.proxy.scheduler
            val future = CompletableFuture<Unit>()
            val task: BungeePlatformTask
            val scheduledTask = when {
                runnable.now -> {
                    task = BungeePlatformTask(future)
                    runnable.executor(task)
                    null
                }
                runnable.period > 0 -> if (runnable.async) {
                    scheduler.schedule(plugin, object : Runnable {
                        init {
                            task = BungeePlatformTask(future)
                        }
                        override fun run() {
                            scheduler.runAsync(plugin) {
                                runnable.executor(task)
                            }
                        }
                    }, runnable.delay, runnable.period * 50, TimeUnit.MILLISECONDS)
                } else {
                    scheduler.schedule(plugin, object : Runnable {
                        init {
                            task = BungeePlatformTask(future)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }, runnable.delay, runnable.period * 50, TimeUnit.MILLISECONDS)
                }
                runnable.delay > 0 -> if (runnable.async) {
                    scheduler.schedule(plugin, object : Runnable {
                        init {
                            task = BungeePlatformTask(future)
                        }
                        override fun run() {
                            scheduler.runAsync(plugin) {
                                runnable.executor(task)
                            }
                        }
                    }, runnable.delay, 0, TimeUnit.MILLISECONDS)
                } else {
                    scheduler.schedule(plugin, object : Runnable {
                        init {
                            task = BungeePlatformTask(future)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }, runnable.delay, 0, TimeUnit.MILLISECONDS)
                }
                else -> if (runnable.async) {
                    scheduler.runAsync(plugin, object : Runnable {
                        init {
                            task = BungeePlatformTask(future)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    })
                } else {
                    scheduler.schedule(plugin, object : Runnable {
                        init {
                            task = BungeePlatformTask(future)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }, 0, 0, TimeUnit.MILLISECONDS)
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

    class BungeePlatformTask(private val future: CompletableFuture<Unit>) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            future.complete(null)
        }
    }
}