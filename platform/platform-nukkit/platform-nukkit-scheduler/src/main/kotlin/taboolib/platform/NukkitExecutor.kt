package taboolib.platform

import cn.nukkit.scheduler.NukkitRunnable
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformRunnable
import taboolib.common.platform.service.PlatformTask

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

    private val tasks = ArrayList<PlatformRunnable>()
    private var started = false

    val plugin by lazy {
        NukkitPlugin.getInstance()
    }

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
    }

    override fun submit(runnable: PlatformRunnable): PlatformTask {
        if (started) {
            var task: NukkitPlatformTask? = null
            fun createTask(): NukkitRunnable {
                return object : NukkitRunnable() {
                    init {
                        task = NukkitPlatformTask(this)
                    }

                    override fun run() {
                        runnable.executor(task!!)
                    }
                }
            }
            when {
                runnable.period > 0 -> if (runnable.async) {
                    createTask().runTaskTimerAsynchronously(plugin, runnable.delay.toInt(), runnable.period.toInt())
                } else {
                    createTask().runTaskTimer(plugin, runnable.delay.toInt(), runnable.period.toInt())
                }
                runnable.delay > 0 -> if (runnable.async) {
                    createTask().runTaskLaterAsynchronously(plugin, runnable.delay.toInt())
                } else {
                    createTask().runTaskLater(plugin, runnable.delay.toInt())
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