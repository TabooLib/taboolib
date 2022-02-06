package taboolib.platform

import org.bukkit.scheduler.BukkitRunnable
import taboolib.internal.Internal
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformRunnable
import taboolib.common.platform.service.PlatformTask

/**
 * TabooLib
 * taboolib.platform.BukkitExecutor
 *
 * @author sky
 * @since 2021/6/15 11:17 下午
 */
@Internal
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitExecutor : PlatformExecutor {

    private var started = false
    private val tasks = ArrayList<PlatformRunnable>()

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformRunnable): PlatformTask {
        if (started) {
            var task: BukkitPlatformTask? = null
            fun createTask(): BukkitRunnable {
                return object : BukkitRunnable() {
                    init {
                        task = BukkitPlatformTask(this)
                    }
                    override fun run() {
                        runnable.executor(task!!)
                    }
                }
            }
            when {
                runnable.period > 0 -> if (runnable.async) {
                    createTask().runTaskTimerAsynchronously(plugin, runnable.delay, runnable.period)
                } else {
                    createTask().runTaskTimer(plugin, runnable.delay, runnable.period)
                }
                runnable.delay > 0 -> if (runnable.async) {
                    createTask().runTaskLaterAsynchronously(plugin, runnable.delay)
                } else {
                    createTask().runTaskLater(plugin, runnable.delay)
                }
                else -> if (runnable.async) {
                    createTask().runTaskAsynchronously(plugin)
                } else {
                    createTask().runTask(plugin)
                }
            }
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