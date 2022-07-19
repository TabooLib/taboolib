package taboolib.platform

import cn.nukkit.scheduler.NukkitRunnable
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.util.unsafeLazy

/**
 * TabooLib
 * taboolib.platform.NukkitExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:37
 */
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitExecutor : PlatformExecutor {

    private val tasks = ArrayList<PlatformExecutor.PlatformRunnable>()
    private var started = false

    val plugin by unsafeLazy {
        NukkitPlugin.getInstance()
    }

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformExecutor.PlatformRunnable): PlatformExecutor.PlatformTask {
        if (started) {
            val task: NukkitPlatformTask
            when {
                runnable.now -> {
                    object : NukkitRunnable() {
                        init {
                            task = NukkitPlatformTask(this)
                            runnable.executor(task)
                        }
                        override fun run() {
                        }
                    }
                }
                runnable.period > 0 -> if (runnable.async) {
                    object : NukkitRunnable() {
                        init {
                            task = NukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskTimerAsynchronously(plugin, runnable.delay.toInt(), runnable.period.toInt())
                } else {
                    object : NukkitRunnable() {
                        init {
                            task = NukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskTimer(plugin, runnable.delay.toInt(), runnable.period.toInt())
                }
                runnable.delay > 0 -> if (runnable.async) {
                    object : NukkitRunnable() {
                        init {
                            task = NukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskLaterAsynchronously(plugin, runnable.delay.toInt())
                } else {
                    object : NukkitRunnable() {
                        init {
                            task = NukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskLater(plugin, runnable.delay.toInt())
                }
                else -> if (runnable.async) {
                    object : NukkitRunnable() {
                        init {
                            task = NukkitPlatformTask(this)
                        }
                        override fun run() {
                            runnable.executor(task)
                        }
                    }.runTaskAsynchronously(plugin)
                } else {
                    object : NukkitRunnable() {
                        init {
                            task = NukkitPlatformTask(this)
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

    class NukkitPlatformTask(val runnable: NukkitRunnable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.cancel()
        }
    }
}