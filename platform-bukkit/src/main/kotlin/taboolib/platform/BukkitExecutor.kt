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

    private val plugin = JavaPlugin.getProvidingPlugin(BukkitIO::class.java) as BukkitPlugin

    override fun execute(async: Boolean, delay: Long, period: Long, executor: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformTask {
        val task: BukkitPlatformTask
        when {
            period > 0 -> if (async) {
                object : BukkitRunnable() {

                    init {
                        task = BukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskTimerAsynchronously(plugin, delay, period)
            } else {
                object : BukkitRunnable() {

                    init {
                        task = BukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskTimer(plugin, delay, period)
            }
            delay > 0 -> if (async) {
                object : BukkitRunnable() {

                    init {
                        task = BukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskLaterAsynchronously(plugin, delay)
            } else {
                object : BukkitRunnable() {

                    init {
                        task = BukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskLater(plugin, delay)
            }
            else -> if (async) {
                object : BukkitRunnable() {

                    init {
                        task = BukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTaskAsynchronously(plugin)
            } else {
                object : BukkitRunnable() {

                    init {
                        task = BukkitPlatformTask(this)
                    }

                    override fun run() {
                        executor(task)
                    }
                }.runTask(plugin)
            }
        }
        return task
    }

    class BukkitPlatformTask(val runnable: BukkitRunnable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.cancel()
        }
    }
}