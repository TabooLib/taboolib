package taboolib.platform

import cn.nukkit.scheduler.NukkitRunnable
import taboolib.common.platform.PlatformExecutor
import taboolib.common.platform.Awake

/**
 * TabooLib
 * taboolib.platform.NukkitExecutor
 *
 * @author CziSKY
 * @since 2021/6/16 0:37
 */
@Awake
class NukkitExecutor : PlatformExecutor {

    val plugin = NukkitPlugin.instance

    override fun execute(async: Boolean, delay: Long, period: Long, executor: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformTask {
        val task: NukkitPlatformTask
        when {
            period > 0 -> if (async) {
                object : NukkitRunnable() {

                    init {
                        task = NukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskTimerAsynchronously(plugin, delay.toInt(), period.toInt())
            } else {
                object : NukkitRunnable() {

                    init {
                        task = NukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskTimer(plugin, delay.toInt(), period.toInt())
            }
            delay > 0 -> if (async) {
                object : NukkitRunnable() {

                    init {
                        task = NukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskLaterAsynchronously(plugin, delay.toInt())
            } else {
                object : NukkitRunnable() {

                    init {
                        task = NukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskLater(plugin, delay.toInt())
            }
            else -> if (async) {
                object : NukkitRunnable() {

                    init {
                        task = NukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskAsynchronously(plugin)
            } else {
                object : NukkitRunnable() {

                    init {
                        task = NukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTask(plugin)
            }
        }
        return task
    }

    class NukkitPlatformTask(val runnable: NukkitRunnable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.cancel()
        }
    }
}