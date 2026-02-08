package taboolib.common.function

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

abstract class ThrottleFunction<K : Any>(
    val keyType: Class<K>,
    val delay: Long,
) {

    val throttleMap = ConcurrentHashMap<K, Long>()

    /**
     * 检查指定键是否可以执行操作
     *
     * 通过比较当前时间与上次执行时间的差值来判断是否满足节流条件。
     * 如果满足条件，会更新该键的最后执行时间并返回 true。
     *
     * @param key 要检查的键
     * @param delay 节流延迟时间（毫秒），默认使用构造时设定的延迟时间
     * @return 如果可以执行则返回 true，否则返回 false
     */
    open fun canExecute(key: K, delay: Long = this.delay): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastExecuteTime = throttleMap.getOrDefault(key, 0L)
        return if (currentTime - lastExecuteTime >= delay) {
            throttleMap[key] = currentTime
            true
        } else false
    }

    /**
     * 移除指定键的节流记录
     * @param key 要移除的节流记录的键
     */
    fun removeKey(key: Any) {
        throttleMap.remove(key)
    }

    /**
     * 清除所有节流记录
     * 清空节流映射表中的所有记录
     */
    fun clearAll() {
        throttleMap.clear()
    }

    /**
     * 无键节流函数类
     *
     * @param delay 节流延迟时间（毫秒）
     * @param action 要执行的操作，接收一个类型为 K 的参数
     */
    class Singleton(
        delay: Long,
        val action: () -> Unit,
    ) : ThrottleFunction<Unit>(Unit::class.java, delay) {

        private var lastExecuteTime = 0L

        init {
            addThrottleFunction(this)
        }

        fun canExecute(delay: Long = 0L): Boolean {
            return canExecute(Unit, delay)
        }

        override fun canExecute(key: Unit, delay: Long): Boolean {
            val currentTime = System.currentTimeMillis()
            return if (currentTime - lastExecuteTime >= delay) {
                lastExecuteTime = currentTime
                true
            } else false
        }

        /**
         * 调用节流函数
         * @param delay 节流延迟时间（毫秒），默认使用构造时设定的延迟时间
         */
        operator fun invoke(delay: Long = this.delay) {
            if (canExecute(Unit, delay)) action()
        }
    }

    /**
     * 简单节流函数类
     *
     * @param K 节流函数键的类型
     * @param keyType 键类型的 Class 对象
     * @param delay 节流延迟时间（毫秒）
     * @param action 要执行的操作，接收一个类型为 K 的参数
     */
    class Simple<K : Any>(
        keyType: Class<K>,
        delay: Long,
        val action: (K) -> Unit,
    ) : ThrottleFunction<K>(keyType, delay) {

        init {
            addThrottleFunction(this)
        }

        /**
         * 调用节流函数
         *
         * @param key 节流函数的键，用于标识不同的调用对象
         * @param delay 节流延迟时间（毫秒），默认使用构造时设定的延迟时间
         */
        operator fun invoke(key: K, delay: Long = this.delay) {
            if (canExecute(key, delay)) action(key)
        }
    }

    /**
     * 带参数的节流函数类
     *
     * @param K 节流函数键的类型
     * @param T 参数的类型
     * @param keyType 键类型的 Class 对象
     * @param delay 节流延迟时间（毫秒）
     * @param action 要执行的操作，接收一个类型为 K 的键和一个类型为 T 的参数
     */
    class Parameterized<K : Any, T>(
        keyType: Class<K>,
        delay: Long,
        val action: (K, T) -> Unit,
    ) : ThrottleFunction<K>(keyType, delay) {

        init {
            addThrottleFunction(this)
        }

        /**
         * 调用带参数的节流函数
         *
         * @param key 节流函数的键，用于标识不同的调用对象
         * @param param 传递给执行函数的参数
         * @param delay 节流延迟时间（毫秒），默认使用构造时设定的延迟时间
         */
        operator fun invoke(key: K, param: T, delay: Long = this.delay) {
            if (canExecute(key, delay)) action(key, param)
        }
    }

    companion object {

        // 所有被创建的节流函数
        val allThrottleFunctions = CopyOnWriteArrayList<ThrottleFunction<*>>()

        // 添加节流函数到列表
        fun addThrottleFunction(throttleFunction: ThrottleFunction<*>) {
            allThrottleFunctions.add(throttleFunction)
        }
    }
}

/**
 * 创建一个简单的节流函数：
 * 不需要指定对象，直接执行操作。在指定时间内只执行一次函数，忽略这段时间内的重复调用。
 *
 * 示例：
 * ```kotlin
 * // 创建一个 500 毫秒的节流函数
 * val throttledAction = throttle(500) {
 *     println("节流后输出")
 * }
 *
 * // 连续调用
 * throttledAction()
 * throttledAction() // 会被忽略
 * throttledAction() // 会被忽略
 *
 * // 等待 600 毫秒后
 * Thread.sleep(600)
 * throttledAction() // 会被执行
 *
 * // 最终只会输出两次：
 * // 节流后输出
 * // 节流后输出
 * ```
 *
 * @param delay 节流时间（单位：毫秒）
 * @param action 要执行的操作
 */
fun throttle(delay: Long, action: () -> Unit = { }): ThrottleFunction.Singleton {
    return ThrottleFunction.Singleton(delay, action)
}

/**
 * 创建基础节流函数：
 * 针对特定对象（如玩家）使用。在指定时间内只执行一次函数，忽略这段时间内的重复调用。
 *
 * 示例：
 * ```kotlin
 * // 创建一个 500 毫秒的节流函数
 * val throttledAction = throttle<Player>(500) { player ->
 *     println("玩家 ${player.name} 的节流后输出")
 * }
 *
 * // 连续调用
 * throttledAction(player)
 * throttledAction(player) // 会被忽略
 * throttledAction(player) // 会被忽略
 *
 * // 等待 600 毫秒后
 * Thread.sleep(600)
 * throttledAction(player) // 会被执行
 *
 * // 最终只会输出两次：
 * // 玩家 player 的节流后输出
 * // 玩家 player 的节流后输出
 * ```
 *
 * @param K 键类型（可以是 Player 或其他对象类型）
 * @param delay 节流时间（单位：毫秒）
 * @param action 要执行的操作
 */
inline fun <reified K : Any> throttle(delay: Long, noinline action: (K) -> Unit = { _ -> }): ThrottleFunction.Simple<K> {
    return ThrottleFunction.Simple(K::class.java, delay, action)
}

/**
 * 创建带参数的节流函数：
 * 针对特定对象（如玩家）使用。在指定时间内只执行一次函数，忽略这段时间内的重复调用。
 * 与基础版本不同的是，这个版本可以传递额外的参数。
 *
 * 示例：
 * ```kotlin
 * // 创建一个 500 毫秒的节流函数
 * val throttledAction = throttle<Player, String>(500) { player, message ->
 *     println("玩家 ${player.name} 的节流后输出：$message")
 * }
 *
 * // 连续调用
 * throttledAction(player, "消息1")
 * throttledAction(player, "消息2") // 会被忽略
 * throttledAction(player, "消息3") // 会被忽略
 *
 * // 等待 600 毫秒后
 * Thread.sleep(600)
 * throttledAction(player, "消息4") // 会被执行
 *
 * // 最终只会输出两次：
 * // 玩家 player 的节流后输出：消息1
 * // 玩家 player 的节流后输出：消息4
 * ```
 *
 * @param K 键类型（可以是 Player 或其他对象类型）
 * @param T 参数类型
 * @param delay 节流时间（单位：毫秒）
 * @param action 要执行的操作
 */
inline fun <reified K : Any, T> throttle(delay: Long, noinline action: (K, T) -> Unit = { _, _ -> }): ThrottleFunction.Parameterized<K, T> {
    return ThrottleFunction.Parameterized(K::class.java, delay, action)
}