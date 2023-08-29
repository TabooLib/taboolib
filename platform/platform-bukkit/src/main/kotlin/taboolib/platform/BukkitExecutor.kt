package taboolib.platform

import org.bukkit.scheduler.BukkitRunnable
import taboolib.common.classloader.IsolatedClassLoader
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor

/**
 * TabooLib
 * taboolib.platform.BukkitExecutor
 *
 * @author sky
 * @since 2021/6/15 11:17 下午
 */
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitExecutor : PlatformExecutor {

    private val tasks = ArrayList<PlatformExecutor.PlatformRunnable>()
    private var started = false

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        val runnableExecutor: PlatformExecutor.PlatformTask.() -> Unit = if (IsolatedClassLoader.isEnabled()) {
            {
                BukkitPlugin.getIsolatedClassLoader()?.runIsolated { runnable.executor(this) }
            }
        } else runnable.executor
        
        if (started) {
            val task: BukkitPlatformTask
            when {
                runnable.now -> {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                            runnableExecutor(task)
                        }
                        override fun run() {
                        }
                    }
                }
                runnable.period > 0 -> if (runnable.async) {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnableExecutor(task)
                        }
                    }.runTaskTimerAsynchronously(plugin, runnable.delay, runnable.period)
                } else {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnableExecutor(task)
                        }
                    }.runTaskTimer(plugin, runnable.delay, runnable.period)
                }
                runnable.delay > 0 -> if (runnable.async) {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnableExecutor(task)
                        }
                    }.runTaskLaterAsynchronously(plugin, runnable.delay)
                } else {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnableExecutor(task)
                        }
                    }.runTaskLater(plugin, runnable.delay)
                }
                else -> if (runnable.async) {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnableExecutor(task)
                        }
                    }.runTaskAsynchronously(plugin)
                } else {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnableExecutor(task)
                        }
                    }.runTask(plugin)
                }
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

    class BukkitPlatformTask(val runnable: BukkitRunnable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.cancel()
        }
    }
}