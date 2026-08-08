package taboolib.platform

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.scheduler.BukkitRunnable
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.PrimitiveSettings
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.service.PlatformExecutor
import java.io.Closeable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TabooLib
 * taboolib.platform.BukkitExecutor
 *
 * @author sky
 * @since 2021/6/15 11:17 下午
 */
@Awake
@Inject
@PlatformSide(Platform.BUKKIT)
class BukkitExecutor : PlatformExecutor {

    private val tasks = ArrayList<RunningTask>()
    private var started = false
    private val contextFreeSyncWarned = AtomicBoolean(false)

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    @Awake(LifeCycle.ENABLE)
    override fun start() {
        started = true
        // 提交列队中的任务
        tasks.forEach {
            if (it.runnable.now) {
                it.execute()
            } else {
                it.execute(it.runnable.async, it.runnable.delay, it.runnable.period)
            }
        }
        tasks.clear()
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
        // Folia 上不存在「无上下文的同步任务」，此类任务回退到全局区域调度器执行。
        // 直接抛出异常会让 @Schedule 等注解驱动的任务静默失效（ClassVisitorHandler 只打印堆栈不中断），
        // 因此这里改为兜底执行并在首次命中时提示一次，便于插件作者迁移到
        // Location.submit() / Entity.submit() / submitGlobal()。
        if (Folia.isFolia && !runnable.now && !runnable.async) {
            warnContextFreeSyncTask()
        }
        // 服务器已启动
        val task = createRunningTask(runnable)
        return if (started) {
            if (runnable.now) {
                task.execute()
            } else {
                task.execute(runnable.async, runnable.delay, runnable.period)
            }
            task.platformTask()
        } else {
            tasks += task
            BukkitPlatformTask {
                if (!task.runnable.now) {
                    task.platformTask().cancel()
                }
                tasks -= task
            }
        }
    }

    private fun warnContextFreeSyncTask() {
        if (!contextFreeSyncWarned.compareAndSet(false, true)) {
            return
        }
        PrimitiveIO.warning(
            "Context-free synchronous tasks are executed on the global region scheduler under Folia. " +
                "Use Location.submit(), Entity.submit(), or submitGlobal() to make the execution context explicit."
        )
        Thread.dumpStack()
    }

    fun createRunningTask(runnable: PlatformExecutor.PlatformRunnable): RunningTask {
        return if (Folia.isFolia) FoliaRunningTask(runnable) else BukkitRunningTask(runnable)
    }

    abstract class RunningTask(val runnable: PlatformExecutor.PlatformRunnable) {

        /** 运行 */
        abstract fun execute()

        /** 运行 */
        abstract fun execute(async: Boolean, delay: Long, period: Long)

        /** 获取跨平台接口 */
        abstract fun platformTask(): PlatformExecutor.PlatformTask
    }

    class BukkitRunningTask(runnable: PlatformExecutor.PlatformRunnable) : RunningTask(runnable) {

        val instance = object : BukkitRunnable() {

            override fun run() {
                runnable.executor(BukkitPlatformTask { cancel() })
            }
        }

        override fun execute() {
            runnable.executor(BukkitPlatformTask { })
        }

        override fun execute(async: Boolean, delay: Long, period: Long) {
            if (async) {
                if (period < 1) {
                    instance.runTaskLaterAsynchronously(BukkitPlugin.getInstance(), delay)
                } else {
                    instance.runTaskTimerAsynchronously(BukkitPlugin.getInstance(), delay, period)
                }
            } else {
                if (period < 1) {
                    instance.runTaskLater(BukkitPlugin.getInstance(), delay)
                } else {
                    instance.runTaskTimer(BukkitPlugin.getInstance(), delay, period)
                }
            }
        }

        override fun platformTask(): PlatformExecutor.PlatformTask {
            return BukkitPlatformTask { instance.cancel() }
        }
    }

    class FoliaRunningTask(runnable: PlatformExecutor.PlatformRunnable) : RunningTask(runnable) {

        var scheduledTask: ScheduledTask? = null

        override fun execute() {
            runnable.executor(BukkitPlatformTask { })
        }

        override fun execute(async: Boolean, delay: Long, period: Long) {
            // 异步任务交由异步调度器；无上下文的同步任务回退到全局区域调度器，
            // 以保证 @Schedule 等注解驱动的任务在 Folia 上依然生效。
            scheduledTask = if (async) {
                if (period < 1) {
                    if (delay < 1) {
                        FoliaExecutor.ASYNC_SCHEDULER.runNow(BukkitPlugin.getInstance()) { task ->
                            runnable.executor(BukkitPlatformTask { task.cancel() })
                        }
                    } else {
                        FoliaExecutor.ASYNC_SCHEDULER.runDelayed(BukkitPlugin.getInstance(), { task ->
                            runnable.executor(BukkitPlatformTask { task.cancel() })
                        }, delay.coerceAtLeast(1) * 50, TimeUnit.MILLISECONDS)
                    }
                } else {
                    FoliaExecutor.ASYNC_SCHEDULER.runAtFixedRate(BukkitPlugin.getInstance(), { task ->
                        runnable.executor(BukkitPlatformTask { task.cancel() })
                    }, delay.coerceAtLeast(1) * 50, period * 50, TimeUnit.MILLISECONDS)
                }
            } else {
                if (period < 1) {
                    if (delay < 1) {
                        FoliaExecutor.GLOBAL_REGION_SCHEDULER.run(BukkitPlugin.getInstance()) { task ->
                            runnable.executor(BukkitPlatformTask { task.cancel() })
                        }
                    } else {
                        FoliaExecutor.GLOBAL_REGION_SCHEDULER.runDelayed(BukkitPlugin.getInstance(), { task ->
                            runnable.executor(BukkitPlatformTask { task.cancel() })
                        }, delay.coerceAtLeast(1))
                    }
                } else {
                    FoliaExecutor.GLOBAL_REGION_SCHEDULER.runAtFixedRate(BukkitPlugin.getInstance(), { task ->
                        runnable.executor(BukkitPlatformTask { task.cancel() })
                    }, delay.coerceAtLeast(1), period)
                }
            }
        }

        override fun platformTask(): PlatformExecutor.PlatformTask {
            return BukkitPlatformTask { scheduledTask?.cancel() }
        }
    }

    class BukkitPlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            runnable.close()
        }
    }
}