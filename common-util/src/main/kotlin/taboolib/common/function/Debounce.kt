package taboolib.common.function

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import java.util.concurrent.*

@Inject
class DebounceFunction<K, T>(
    private val delay: Long,
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    private val action: (K?, T) -> Unit,
    private val autoShutdown: Boolean = true
) {
    private val globalFuture = ConcurrentHashMap.newKeySet<ScheduledFuture<*>>()
    private val futureMap = ConcurrentHashMap<K, ScheduledFuture<*>>()

    init {
        // 将此实例添加到全局列表中
        addDebounceFunction(this)
    }

    /**
     * 全局防抖调用
     *
     * @param param 要传递给 action 的参数
     */
    fun invoke(param: T) {
        invoke(null, param)
    }

    /**
     * 针对特定键的防抖调用
     *
     * @param key 防抖的键，如果为 null 则使用全局防抖
     * @param param 要传递给 action 的参数
     */
    fun invoke(key: K?, param: T) {
        val future = executor.schedule({ action(key, param) }, delay, TimeUnit.MILLISECONDS)
        when (key) {
            null -> {
                globalFuture.forEach { it.cancel(false) }
                globalFuture.clear()
                globalFuture.add(future)
            }

            else -> {
                futureMap[key]?.cancel(false)
                futureMap[key] = future
            }
        }
    }

    /**
     * 移除指定键的防抖状态
     */
    fun removeKey(key: K) {
        futureMap.remove(key)?.cancel(false)
    }

    /**
     * 清除所有防抖状态
     */
    fun clearAll() {
        globalFuture.forEach { it.cancel(false) }
        globalFuture.clear()
        futureMap.values.forEach { it.cancel(false) }
        futureMap.clear()
    }

    /**
     * 关闭执行器
     */
    fun shutdown() {
        clearAll()
        executor.shutdown()
    }

    private companion object {

        // 所有被创建的防抖函数
        private val allDebounceFunctions = mutableListOf<DebounceFunction<*, *>>()

        @Awake(LifeCycle.DISABLE)
        fun onDisable() {
            // 关闭所有需要自动关闭的执行器
            allDebounceFunctions.forEach { debounceFunction ->
                if (debounceFunction.autoShutdown) {
                    debounceFunction.shutdown()
                }
            }
            // 清空列表
            allDebounceFunctions.clear()
        }

        // 添加防抖函数到列表
        fun addDebounceFunction(debounceFunction: DebounceFunction<*, *>) {
            allDebounceFunctions.add(debounceFunction)
        }
    }
}

/**
 * 创建防抖函数：
 * 可以全局使用，也可以针对特定对象（如玩家）使用。在指定时间内只执行一次函数，如果在这段时间内再次调用函数，则重新计时。
 *
 * 示例：
 * ```kotlin
 * // 创建一个 500 毫秒的防抖函数
 * val debouncedAction = debounce<Player?, String>(500) { player, message ->
 *     when (player) {
 *         null -> println("全局防抖后输出：$message")
 *         else -> println("玩家 ${player.name} 的防抖后输出：$message")
 *     }
 * }
 *
 * // 全局使用
 * debouncedAction(null, "全局测试1")
 * debouncedAction(null, "全局测试2")
 *
 * // 针对玩家使用
 * debouncedAction(player1, "测试1")
 * debouncedAction(player1, "测试2")
 * debouncedAction(player2, "测试A")
 *
 * // 等待 600 毫秒
 * Thread.sleep(600)
 *
 * // 最终会输出：
 * // 全局防抖后输出：全局测试2
 * // 玩家 player1 的防抖后输出：测试2
 * // 玩家 player2 的防抖后输出：测试A
 * ```
 *
 * @param K 键类型（可以是 Player 或其他对象类型）
 * @param T 参数类型
 * @param delay 防抖时间（单位：毫秒）
 * @param executor 自定义的执行器，默认使用单线程调度执行器
 * @param autoShutdown 是否在插件禁用时自动关闭执行器，默认为 true
 */
fun <K, T> debounce(
    delay: Long,
    executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    autoShutdown: Boolean = true,
    action: (K?, T) -> Unit
): (K?, T) -> Unit {
    val debounceFunction = DebounceFunction(delay, executor, action, autoShutdown)
    return { key: K?, param: T -> debounceFunction.invoke(key, param) }
}