package taboolib.common.event

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * TabooLib
 * taboolib.common.event.InternalEventBus
 *
 * @author 坏黑
 * @since 2024/1/26 15:27
 */
interface InternalEventBus {

    /** 判断一个事件是否被监听 */
    fun isListening(cls: Class<*>): Boolean

    /** 唤起事件 */
    fun <T : InternalEvent> call(event: T)

    /** 监听事件 */
    fun <T : InternalEvent> listen(cls: Class<T>, priority: Int, ignoreCancelled: Boolean, listener: (event: T) -> Unit): InternalListener

    companion object {

        /** 判断一个事件是否被监听 */
        fun isListening(cls: Class<*>): Boolean {
            return impl.isListening(cls)
        }

        /** 唤起事件 */
        fun <T : InternalEvent> call(event: T) = impl.call(event)

        /** 监听事件 */
        fun <T : InternalEvent> listen(cls: Class<T>, priority: Int = 0, ignoreCancelled: Boolean = false, listener: (event: T) -> Unit): InternalListener {
            return impl.listen(cls, priority, ignoreCancelled, listener)
        }

        /** 监听事件 */
        inline fun <reified T : InternalEvent> listen(priority: Int = 0, ignoreCancelled: Boolean = false, noinline listener: (event: T) -> Unit): InternalListener {
            return impl.listen(T::class.java, priority, ignoreCancelled, listener)
        }

        /** 默认实现 */
        var impl = object : InternalEventBus {

            /** 已注册的监听器 */
            val registeredListeners = ConcurrentHashMap<Class<*>, MutableMap<Int, MutableList<RegisteredListener>>>()

            override fun isListening(cls: Class<*>): Boolean {
                return registeredListeners.containsKey(cls) && registeredListeners[cls]!!.any { it.value.isNotEmpty() }
            }

            override fun <T : InternalEvent> call(event: T) {
                registeredListeners[event.javaClass]?.entries?.flatMap { it.value }?.forEach { listener ->
                    // 如果事件可取消 & 事件已被取消 & 监听器忽略取消事件
                    if (event is CancelableInternalEvent && event.isCancelled && listener.ignoreCancelled) {
                        return@forEach
                    }
                    // 运行函数
                    listener.invoke(event)
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun <T : InternalEvent> listen(cls: Class<T>, priority: Int, ignoreCancelled: Boolean, listener: (event: T) -> Unit): InternalListener {
                val registeredListener = RegisteredListener(cls, priority, ignoreCancelled, listener as (Any) -> Unit)
                registeredListeners.getOrPut(cls) { ConcurrentSkipListMap() }.getOrPut(priority) { CopyOnWriteArrayList() }.add(registeredListener)
                return registeredListener
            }

            /** 已注册的监听器 */
            inner class RegisteredListener(val cls: Class<*>, val priority: Int, val ignoreCancelled: Boolean, val listener: (event: Any) -> Unit) : InternalListener {

                override fun cancel() {
                    registeredListeners[cls]?.get(priority)?.remove(this)
                }

                fun invoke(event: Any) {
                    listener(event)
                }
            }
        }
    }
}