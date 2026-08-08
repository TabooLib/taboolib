package taboolib.common.function

import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

abstract class DebounceFunction<K : Any>(
    val keyType: Class<K>,
    val delay: Long,
    val async: Boolean = false,
) {
    val futureMap = ConcurrentHashMap<K, PlatformExecutor.PlatformTask>()
    val tokenMap = ConcurrentHashMap<K, Any>()

    /**
     * 移除指定键的防抖任务
     * @param key 要移除的防抖任务的键
     */
    fun removeKey(key: Any) {
        tokenMap.remove(key)
        futureMap.remove(key)?.cancel()
    }

    /**
     * 清除所有防抖任务
     * 取消所有正在执行的任务并清空任务映射表
     */
    open fun clearAll() {
        tokenMap.clear()
        futureMap.values.forEach { it.cancel() }
        futureMap.clear()
    }

    /**
     * 无键防抖函数类
     *
     * @param delay 延迟执行的时间（毫秒）
     * @param async 是否异步执行，默认为 false
     * @param action 要执行的操作，接收一个类型为 K 的参数
     */
    class Singleton(
        delay: Long,
        async: Boolean = false,
        val action: () -> Unit,
    ) : DebounceFunction<Unit>(Unit::class.java, delay, async) {

        var task: PlatformExecutor.PlatformTask? = null

        /**
         * 取消待执行的防抖任务。
         *
         * Singleton 的状态存于 [task] 而非父类的 tokenMap / futureMap，
         * 因此必须覆写，否则调用父类实现对本类毫无效果。
         */
        override fun clearAll() {
            task?.cancel()
            task = null
        }

        /**
         * 调用防抖函数
         * @param delay 延迟时间（毫秒），默认使用构造时设定的延迟时间
         */
        operator fun invoke(delay: Long = this.delay) {
            val future = submit(async = async, delay = delay / 50) {
                try {
                    action()
                } finally {
                    if (task === this) {
                        task = null
                    }
                }
            }
            task?.cancel()
            task = future
        }
    }

    /**
     * 简单防抖函数类
     *
     * @param K 防抖函数键的类型
     * @param keyType 键类型的 Class 对象
     * @param delay 延迟执行的时间（毫秒）
     * @param async 是否异步执行，默认为 false
     * @param action 要执行的操作，接收一个类型为 K 的参数
     */
    class Simple<K : Any>(
        keyType: Class<K>,
        delay: Long,
        async: Boolean = false,
        val action: (K) -> Unit,
    ) : DebounceFunction<K>(keyType, delay, async) {

        init {
            if (isGlobalType(keyType)) {
                addDebounceFunction(this)
            }
        }

        /**
         * 调用防抖函数
         *
         * @param key 防抖函数的键，用于标识不同的调用对象
         * @param delay 延迟时间（毫秒），默认使用构造时设定的延迟时间
         */
        operator fun invoke(key: K, delay: Long = this.delay) {
            val token = Any()
            tokenMap[key] = token
            val future = submit(async = async, delay = delay / 50) {
                try {
                    if (tokenMap[key] === token) {
                        action(key)
                    }
                } finally {
                    if (tokenMap.remove(key, token)) {
                        futureMap.remove(key)
                    }
                }
            }
            futureMap.put(key, future)?.cancel()
            if (tokenMap[key] !== token) {
                future.cancel()
                futureMap.remove(key, future)
            }
        }
    }

    /**
     * 带参数的防抖函数类
     *
     * @param K 防抖函数键的类型
     * @param T 参数的类型
     * @param keyType 键类型的 Class 对象
     * @param delay 延迟执行的时间（毫秒）
     * @param async 是否异步执行，默认为 false
     * @param action 要执行的操作，接收一个类型为 K 的键和一个类型为 T 的参数
     */
    class Parameterized<K : Any, T>(
        keyType: Class<K>,
        delay: Long,
        async: Boolean = false,
        val action: (K, T) -> Unit,
    ) : DebounceFunction<K>(keyType, delay, async) {

        init {
            if (isGlobalType(keyType)) {
                addDebounceFunction(this)
            }
        }

        /**
         * 调用带参数的防抖函数
         *
         * @param key 防抖函数的键，用于标识不同的调用对象
         * @param param 传递给执行函数的参数
         * @param delay 延迟时间（毫秒），默认使用构造时设定的延迟时间
         */
        operator fun invoke(key: K, param: T, delay: Long = this.delay) {
            val token = Any()
            tokenMap[key] = token
            val future = submit(async = async, delay = delay / 50) {
                try {
                    if (tokenMap[key] === token) {
                        action(key, param)
                    }
                } finally {
                    if (tokenMap.remove(key, token)) {
                        futureMap.remove(key)
                    }
                }
            }
            futureMap.put(key, future)?.cancel()
            if (tokenMap[key] !== token) {
                future.cancel()
                futureMap.remove(key, future)
            }
        }
    }

    companion object {

        val allDebounceFunctions = CopyOnWriteArrayList<DebounceFunction<*>>()

        val globalTypes = CopyOnWriteArrayList<Class<*>>()

        fun registerGlobalType(type: Class<*>) {
            if (type !in globalTypes) {
                globalTypes += type
            }
        }

        fun isGlobalType(type: Class<*>): Boolean {
            return globalTypes.any { it == type }
        }

        fun addDebounceFunction(debounceFunction: DebounceFunction<*>) {
            allDebounceFunctions.add(debounceFunction)
        }
    }
}

/**
 * 创建一个简单的防抖函数：
 * 不需要指定对象，直接执行操作。在指定时间内只执行一次函数，如果在这段时间内再次调用函数，则重新计时。
 *
 * 示例：
 * ```kotlin
 * // 创建一个 500 毫秒的防抖函数
 * val debouncedAction = debounce(500) {
 *     println("防抖后输出")
 * }
 *
 * // 连续调用
 * debouncedAction()
 * debouncedAction() // 重置计时
 * debouncedAction() // 重置计时
 *
 * // 等待 600 毫秒
 * Thread.sleep(600)
 *
 * // 最终只会输出一次：
 * // 防抖后输出
 * ```
 *
 * @param delay 防抖时间（单位：毫秒）
 * @param async 是否在异步线程执行，取决于当前线程
 * @param action 要执行的操作
 */
fun debounce(
    delay: Long,
    async: Boolean = !isPrimaryThread,
    action: () -> Unit = { },
): DebounceFunction.Singleton {
    return DebounceFunction.Singleton(delay, async, action)
}

/**
 * 创建基础防抖函数：
 * 针对特定对象（如玩家）使用。在指定时间内只执行一次函数，如果在这段时间内再次调用函数，则重新计时。
 *
 * 示例：
 * ```kotlin
 * // 创建一个 500 毫秒的防抖函数
 * val debouncedAction = debounce<Player>(500) { player ->
 *     println("玩家 ${player.name} 的防抖后输出")
 * }
 *
 * // 连续调用
 * debouncedAction(player)
 * debouncedAction(player) // 重置计时
 * debouncedAction(player) // 重置计时
 *
 * // 等待 600 毫秒
 * Thread.sleep(600)
 *
 * // 最终只会输出一次：
 * // 玩家 player 的防抖后输出
 * ```
 *
 * @param K 键类型（可以是 Player 或其他对象类型）
 * @param delay 防抖时间（单位：毫秒）
 * @param async 是否在异步线程执行，取决于当前线程
 * @param action 要执行的操作
 */
inline fun <reified K : Any> debounce(
    delay: Long,
    async: Boolean = !isPrimaryThread,
    noinline action: (K) -> Unit = { _ -> },
): DebounceFunction.Simple<K> {
    return DebounceFunction.Simple(K::class.java, delay, async, action)
}

/**
 * 创建带参数的防抖函数：
 * 针对特定对象（如玩家）使用。在指定时间内只执行一次函数，如果在这段时间内再次调用函数，则重新计时。
 * 与基础版本不同的是，这个版本可以传递额外的参数。
 *
 * 示例：
 * ```kotlin
 * // 创建一个 500 毫秒的防抖函数
 * val debouncedAction = debounce<Player, String>(500) { player, message ->
 *     println("玩家 ${player.name} 的防抖后输出：$message")
 * }
 *
 * // 连续调用
 * debouncedAction(player, "消息1")
 * debouncedAction(player, "消息2") // 重置计时
 * debouncedAction(player, "消息3") // 重置计时
 *
 * // 等待 600 毫秒
 * Thread.sleep(600)
 *
 * // 最终只会输出一次：
 * // 玩家 player 的防抖后输出：消息3
 * ```
 *
 * @param K 键类型（可以是 Player 或其他对象类型）
 * @param T 参数类型
 * @param delay 防抖时间（单位：毫秒）
 * @param async 是否在异步线程执行，取决于当前线程
 * @param action 要执行的操作
 */
inline fun <reified K : Any, T> debounce(
    delay: Long,
    async: Boolean = !isPrimaryThread,
    noinline action: (K, T) -> Unit = { _, _ -> },
): DebounceFunction.Parameterized<K, T> {
    return DebounceFunction.Parameterized(K::class.java, delay, async, action)
}
