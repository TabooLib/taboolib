package taboolib.platform

import org.bukkit.scheduler.BukkitRunnable
import taboolib.common.PrimitiveSettings
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.service.PlatformExecutor

/**
 * TabooLib
 * taboolib.platform.BukkitExecutor
 *
 * @author sky
 * @since 2021/6/15 11:17 下午
 */
@Awake
@PlatformSide(Platform.BUKKIT)
class BukkitExecutor : PlatformExecutor {

    private val tasks = ArrayList<PlatformExecutor.PlatformRunnable>()
    private var started = false

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    override fun start() {
        started = true
        tasks.forEach { submit(it) }
        // 启动插件统计
        runCatching {
            val metrics = BukkitMetrics(plugin, "TabooLib-6", 21108, PrimitiveSettings.TABOOLIB_VERSION)
            metrics.addCustomChart(BukkitMetrics.SimplePie("project") { pluginId })
            metrics.addCustomChart(BukkitMetrics.SimplePie("kotlin_version") { PrimitiveSettings.KOTLIN_VERSION })
            metrics.addCustomChart(BukkitMetrics.SimplePie("taboolib_version") { PrimitiveSettings.TABOOLIB_VERSION })
            metrics.addCustomChart(BukkitMetrics.AdvancedPie("install_module") { PrimitiveSettings.INSTALL_MODULES.associateWith { 1 } })
        }
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