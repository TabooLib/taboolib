package taboolib.common.function

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@Inject
class ThrottleFunction<K, T>(
    private val delay: Long,
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    private val action: (K?, T) -> Unit,
    private val autoShutdown: Boolean = true
) {
    private val globalThrottle = AtomicBoolean(false)
    private val throttleMap = ConcurrentHashMap<K, AtomicBoolean>()

    init {
        // 将此实例添加到全局列表中
        addThrottleFunction(this)
    }

    /**
     * 全局节流调用
     *
     * @param param 要传递给 action 的参数
     */
    fun invoke(param: T) {
        invoke(null, param)
    }

    /**
     * 针对特定键的节流调用
     *
     * @param key 节流的键，如果为 null 则使用全局节流
     * @param param 要传递给 action 的参数
     */
    fun invoke(key: K?, param: T) {
        val isThrottled = when (key) {
            null -> globalThrottle
            else -> throttleMap.computeIfAbsent(key) { AtomicBoolean(false) }
        }

        if (isThrottled.compareAndSet(false, true)) {
            action(key, param)
            executor.schedule({ isThrottled.set(false) }, delay, TimeUnit.MILLISECONDS)
        }
    }

    /**
     * 移除指定键的节流状态
     */
    fun removeKey(key: K) {
        throttleMap.remove(key)
    }

    /**
     * 清除所有节流状态
     */
    fun clearAll() {
        globalThrottle.set(false)
        throttleMap.clear()
    }

    /**
     * 关闭执行器
     */
    fun shutdown() {
        executor.shutdown()
    }

    private companion object {

        // 所有被创建的节流函数
        private val allThrottleFunctions = mutableListOf<ThrottleFunction<*, *>>()

        @Awake(LifeCycle.DISABLE)
        fun onDisable() {
            // 关闭所有需要自动关闭的执行器
            allThrottleFunctions.forEach { throttleFunction ->
                if (throttleFunction.autoShutdown) {
                    throttleFunction.shutdown()
                }
            }
            // 清空列表
            allThrottleFunctions.clear()
        }

        // 添加节流函数到列表
        fun addThrottleFunction(throttleFunction: ThrottleFunction<*, *>) {
            allThrottleFunctions.add(throttleFunction)
        }
    }
}

/**
 * 创建节流函数：
 * 可以全局使用，也可以针对特定对象（如玩家）使用。在指定时间内只执行一次函数，如果在这段时间内再次调用函数，则忽略该调用。
 *
 * 示例：
 * ```kotlin
 * // 创建一个 500 毫秒的节流函数
 * val throttledAction = throttle<Player?, String>(500) { player, message ->
 *     when (player) {
 *         null -> println("全局节流后输出：$message")
 *         else -> println("玩家 ${player.name} 的节流后输出：$message")
 *     }
 * }
 *
 * // 全局使用
 * throttledAction(null, "全局测试1")
 * throttledAction(null, "全局测试2")
 *
 * // 针对玩家使用
 * throttledAction(player1, "测试1")
 * throttledAction(player1, "测试2")
 * throttledAction(player2, "测试A")
 *
 * // 等待 600 毫秒
 * Thread.sleep(600)
 *
 * // 再次调用
 * throttledAction(null, "全局测试3")
 * throttledAction(player1, "测试3")
 * throttledAction(player2, "测试B")
 *
 * // 最终会输出：
 * // 全局节流后输出：全局测试1
 * // 玩家 player1 的节流后输出：测试1
 * // 玩家 player2 的节流后输出：测试A
 * // 全局节流后输出：全局测试3
 * // 玩家 player1 的节流后输出：测试3
 * // 玩家 player2 的节流后输出：测试B
 * ```
 *
 * @param K 键类型（可以是 Player 或其他对象类型）
 * @param T 参数类型
 * @param delay 节流时间（单位：毫秒）
 * @param executor 自定义的执行器，默认使用单线程调度执行器
 * @param autoShutdown 是否在插件禁用时自动关闭执行器，默认为 true
 */
fun <K, T> throttle(
    delay: Long,
    executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    autoShutdown: Boolean = true,
    action: (K?, T) -> Unit
): (K?, T) -> Unit {
    val throttleFunction = ThrottleFunction(delay, executor, action, autoShutdown)
    return { key: K?, param: T -> throttleFunction.invoke(key, param) }
}