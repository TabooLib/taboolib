package taboolib.platform.bukkit

import org.bukkit.Bukkit
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.ReflexClass
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.util.t
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 一种在 Bukkit 插件环境下利用 Exchanges 实现的并行初始化设想
 *
 * 正常情况下，插件的初始化是串行的，即插件 A 初始化完成后才会初始化插件 B
 * ```
 * A --> B --> C
 * ```
 *
 * 假如插件 B 在启动过程中创建任何数据库的连接，就会导致整个服务器的启动时间变长
 * 并且插件 C 可能并不需要等待 B 连上数据库才能开始启动
 *
 * 为了解决这个问题，我设想了 Parallel 工具，以提供一种并行列队，来实现 A 和 B 的并行初始化，而 C 仍然可以依赖 A 的数据：
 * ```
 * // 所有插件的初始化列队都必须在 INIT 时注册
 * // 注册一个名为 "task_a" 的任务，在 ENABLE 生命周期下执行，这个任务名必须在所有插件的所有任务中唯一
 * parallel(id = "task_a", runOn = LifeCycle.ENABLE) {
 *   // 加载插件数据
 * }
 *
 * // 注册一个名为匿名任务，立即执行
 * parallel {
 *   // 连接数据库
 * }
 *
 * // 注册一个匿名任务，依赖 "task_a"
 * parallel(dependOn = "task_a") {
 *   // 读取插件 A 的数据
 * }
 * ```
 *
 * 那么，原本的串行初始化就变成了并行初始化：
 * ```
 * A' --> C'
 * B'
 * ```
 *
 * 同时，为了解决并行数据加载完成之前，玩家提前进入服务器的问题
 * 将会借助 Exchanges 系统选举出一个主插件，负责在所有并行任务完成之前阻止玩家进入服务器
 *
 * 注意别引用到其他插件的 parallel 函数。
 * 这个工具仅适用于 Bukkit 插件环境，不适用于 BungeeCord 及其他环境。
 *
 * 还有一种 @Parallel 注解，可以直接使一个方法成为并行任务
 * ```
 * @Parallel
 * fun taskA() {
 *   // 加载插件数据
 * }
 * ```
 */
@Awake
@Inject
@PlatformSide(Platform.BUKKIT)
object ParallelSystem : ClassVisitor(0) {

    val localTaskMap = ConcurrentHashMap<String, Task>()

    val globalTaskMap: MutableMap<String, CompletableFuture<*>>
        get() = Exchanges.getOrPut("parallel_task_map") { ConcurrentHashMap() }

    val executorService: ExecutorService
        get() = Exchanges.getOrPut("parallel_executor_service") { Executors.newFixedThreadPool(Bukkit.getPluginManager().plugins.size * 2) }

    fun registerTask(id: String, dependOn: List<String>, lifeCycle: LifeCycle, block: () -> Unit): CompletableFuture<Unit> {
        // 不允许在 INIT 之后注册
        if (TabooLib.getCurrentLifeCycle() > lifeCycle) {
            error(
                """
                    并行任务必须在 ${lifeCycle.name} 或更早的生命周期下注册。
                    Parallel task must be registered in ${lifeCycle.name} or earlier life cycle.
                """.t()
            )
        }
        // 检查是否存在相同的 ID
        if (globalTaskMap.containsKey(id)) {
            error(
                """
                    并行任务 ID 重复：$id
                    Parallel task ID duplicate: $id
                """.t()
            )
        }
        val task = Task(id, dependOn, lifeCycle, block)
        localTaskMap[id] = task
        globalTaskMap[id] = task.future
        return task.future
    }

    fun runTask(lifeCycle: LifeCycle) {
        // 获取符合生命周期的任务
        val tasks = localTaskMap.values.filter { it.lifeCycle == lifeCycle }
        // 向中心化的 ExecutorService 提交任务
        tasks.forEach { task ->
            if (task.dependOn.isEmpty()) {
                // 没有依赖的任务直接提交到线程池
                executorService.submit {
                    try {
                        task.block()
                        task.future.complete(null)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        task.future.completeExceptionally(e)
                    }
                }
            } else {
                // 有依赖的任务，等待依赖完成后执行
                val dependencies = task.dependOn.map { dependTaskId ->
                    // 软依赖
                    if (dependTaskId.endsWith('?')) {
                        val id = dependTaskId.substring(0, dependTaskId.length - 1)
                        globalTaskMap[id] ?: CompletableFuture.completedFuture(null)
                    } else {
                        globalTaskMap[dependTaskId] ?: error(
                            """
                                并行任务 ${task.id} 的依赖不存在：$dependTaskId
                                Parallel task ${task.id} depends not found: $dependTaskId
                            """.t()
                        )
                    }
                }
                CompletableFuture.allOf(*dependencies.toTypedArray()).thenRunAsync({
                    try {
                        task.block()
                        task.future.complete(null)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        task.future.completeExceptionally(e)
                    }
                }, executorService)
            }
        }
    }

    override fun visit(method: ClassMethod, owner: ReflexClass) {
        if (method.isAnnotationPresent(Parallel::class.java)) {
            val annotation = method.getAnnotation(Parallel::class.java)
            val id = annotation.property("id", "").ifEmpty { "anonymous_${UUID.randomUUID()}" }
            val dependOn = annotation.list<String>("dependOn")
            val lifeCycle = annotation.enum("lifeCycle", LifeCycle.ENABLE)
            registerTask(id, dependOn, lifeCycle) {
                val obj = findInstance(owner)
                if (obj == null) {
                    method.invokeStatic()
                } else {
                    method.invoke(obj)
                }
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.INIT
    }

    @Awake(LifeCycle.INIT)
    private fun onLoad() {
        TabooLib.registerLifeCycleTask(LifeCycle.LOAD, 0) { runTask(LifeCycle.LOAD) }
        TabooLib.registerLifeCycleTask(LifeCycle.ENABLE, 0) { runTask(LifeCycle.ENABLE) }
        TabooLib.registerLifeCycleTask(LifeCycle.ACTIVE, 0) { runTask(LifeCycle.ACTIVE) }
    }

    @Awake(LifeCycle.ENABLE)
    private fun onEnable() {
        // 首个执行到 ENABLE 阶段的插件负责拦截玩家进入
        if (Exchanges.contains("parallel_main_plugin")) {
            return
        }
        Exchanges["parallel_main_plugin"] = pluginId
        registerBukkitListener(AsyncPlayerPreLoginEvent::class.java) {
            if (globalTaskMap.values.any { !it.isDone }) {
                it.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER, """
                        服务器正在启动。
                        Server is starting.
                    """.t()
                )
            }
        }
    }

    class Task(val id: String, val dependOn: List<String>, val lifeCycle: LifeCycle, val block: () -> Any?) {

        val future = CompletableFuture<Unit>()
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Parallel(val id: String = "", val dependOn: Array<String> = [], val runOn: LifeCycle = LifeCycle.ENABLE)

/**
 * 注册一个并行任务
 *
 * @param id 任务 ID，默认为随机 UUID
 * @param runOn 任务执行的生命周期，默认为 ENABLE
 * @param block 任务执行的代码块
 * @return 任务执行的 Future 对象
 */
fun parallel(id: String = "anonymous_${UUID.randomUUID()}", runOn: LifeCycle = LifeCycle.ENABLE, block: () -> Unit): CompletableFuture<Unit> {
    return parallel(id, emptyList(), runOn, block)
}

/**
 * 注册一个带依赖的并行任务
 *
 * @param id 任务 ID，默认为随机 UUID
 * @param dependOn 依赖的任务 ID
 * @param runOn 任务执行的生命周期，默认为 ENABLE
 * @param block 任务执行的代码块
 * @return 任务执行的 Future 对象
 */
fun parallel(id: String = "anonymous_${UUID.randomUUID()}", dependOn: String, runOn: LifeCycle = LifeCycle.ENABLE, block: () -> Unit): CompletableFuture<Unit> {
    return parallel(id, listOf(dependOn), runOn, block)
}

/**
 * 注册一个带多个依赖的并行任务
 *
 * @param id 任务 ID，默认为随机 UUID
 * @param dependOn 依赖的任务 ID 列表
 * @param runOn 任务执行的生命周期，默认为 ENABLE
 * @param block 任务执行的代码块
 * @return 任务执行的 Future 对象
 */
fun parallel(id: String = "anonymous_${UUID.randomUUID()}", dependOn: List<String>, runOn: LifeCycle = LifeCycle.ENABLE, block: () -> Unit): CompletableFuture<Unit> {
    return ParallelSystem.registerTask(id, dependOn, runOn, block)
}