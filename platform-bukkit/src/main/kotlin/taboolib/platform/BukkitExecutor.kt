package taboolib.platform

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformExecutor
import taboolib.common.platform.PlatformSide

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

    val plugin by lazy {
        JavaPlugin.getProvidingPlugin(BukkitIO::class.java) as BukkitPlugin
    }

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        if (started) {
            val task: BukkitPlatformTask
            when {
                runnable.now -> {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                            runnable.executor(task)
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
                            runnable.executor(task)
                        }
                    }.runTaskTimerAsynchronously(plugin, runnable.delay, runnable.period)
                } else {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskTimer(plugin, runnable.delay, runnable.period)
                }
                runnable.delay > 0 -> if (runnable.async) {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskLaterAsynchronously(plugin, runnable.delay)
                } else {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskLater(plugin, runnable.delay)
                }
                else -> if (runnable.async) {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskAsynchronously(plugin)
                } else {
                    object : BukkitRunnable() {
                        init {
                            task = BukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
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