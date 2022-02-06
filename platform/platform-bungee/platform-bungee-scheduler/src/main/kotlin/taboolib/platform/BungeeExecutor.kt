package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformRunnable
import taboolib.common.platform.service.PlatformTask
import taboolib.internal.Internal
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * TabooLib
 * taboolib.platform.BungeeExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:13
 */
@Internal
@Awake
@PlatformSide([Platform.BUNGEE])
class BungeeExecutor : PlatformExecutor {

    private var started = false
    private val tasks = ArrayList<PlatformRunnable>()

    val plugin by lazy {
        BungeePlugin.getInstance()
    }

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformRunnable): PlatformTask {
        if (started) {
            val scheduler = plugin.proxy.scheduler
            val future = CompletableFuture<Unit>()
            var task: BungeePlatformTask? = null
            fun createTask(async: Boolean = false): Runnable {
                return object : Runnable {
                    init {
                        task = BungeePlatformTask(future)
                    }
                    override fun run() {
                        if (async) {
                            scheduler.runAsync(plugin) { runnable.executor(task!!) }
                        } else {
                            runnable.executor(task!!)
                        }
                    }
                }
            }
            val scheduledTask = when {
                runnable.period > 0 -> if (runnable.async) {
                    scheduler.schedule(plugin, createTask(true), runnable.delay, runnable.period * 50, TimeUnit.MILLISECONDS)
                } else {
                    scheduler.schedule(plugin, createTask(), runnable.delay, runnable.period * 50, TimeUnit.MILLISECONDS)
                }
                runnable.delay > 0 -> if (runnable.async) {
                    scheduler.schedule(plugin, createTask(true), runnable.delay, 0, TimeUnit.MILLISECONDS)
                } else {
                    scheduler.schedule(plugin, createTask(), runnable.delay, 0, TimeUnit.MILLISECONDS)
                }
                else -> if (runnable.async) {
                    scheduler.runAsync(plugin, createTask(true))
                } else {
                    scheduler.schedule(plugin, createTask(), 0, 0, TimeUnit.MILLISECONDS)
                }
            }
            future.thenAccept { scheduledTask?.cancel() }
            return task!!
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