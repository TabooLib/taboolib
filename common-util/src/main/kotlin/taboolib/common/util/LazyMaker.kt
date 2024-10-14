package taboolib.common.util

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder

/**
 * 声明一个线程不安全的延迟加载对象
 *
 * @param initializer 初始化函数
 */
fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

/**
 * 声明一个允许重置的延迟加载对象
 *
 * @param groups 组
 * @param synchronized 是否线程安全
 * @param initializer 初始化函数
 */
fun <T> resettableLazy(vararg groups: String, synchronized: Boolean = false, initializer: () -> T): ResettableLazy<T> {
    return if (synchronized) {
        ResettableSynchronizedLazyImpl(*groups, initializer = initializer)
    } else {
        ResettableLazyImpl(*groups, initializer = initializer)
    }.also {
        ResettableLazy.defined.put(it, Unit)
    }
}

abstract class ResettableLazy<T>(vararg val groups: String) : Lazy<T> {

    abstract fun reset()

    companion object {

        val defined: Cache<ResettableLazy<*>, Unit> = CacheBuilder.newBuilder().weakKeys().build()

        fun reset(vararg groups: String) {
            if (groups.isEmpty() || groups.contains("*")) {
                defined.asMap().keys.forEach { it.reset() }
            } else {
                defined.asMap().keys.filter { lazy -> groups.any { lazy.groups.contains(it) } }.forEach { it.reset() }
            }
        }
    }
}

private class ResettableLazyImpl<T>(vararg groups: String, initializer: () -> T) : ResettableLazy<T>(*groups) {

    private var initializer: (() -> T)? = initializer

    private var localValue: Any? = UninitializedValue

    @Suppress("UNCHECKED_CAST")
    override val value: T
        get() {
            if (localValue === UninitializedValue) {
                localValue = initializer!!()
            }
            return localValue as T
        }

    override fun reset() {
        localValue = UninitializedValue
    }

    override fun isInitialized() = localValue !== UninitializedValue

    override fun toString() = if (isInitialized()) value.toString() else "Lazy(${groups.joinToString()}) value not initialized yet."
}

private class ResettableSynchronizedLazyImpl<T>(vararg groups: String, initializer: () -> T) : ResettableLazy<T>(*groups) {

    private var initializer: (() -> T)? = initializer

    @Volatile
    private var localValue: Any? = UninitializedValue

    private val lock = this

    @Suppress("UNCHECKED_CAST")
    override val value: T
        get() {
            val v1 = localValue
            if (v1 !== UninitializedValue) {
                return v1 as T
            }
            return synchronized(lock) {
                val v2 = localValue
                if (v2 !== UninitializedValue) {
                    v2 as T
                } else {
                    val typedValue = initializer!!()
                    localValue = typedValue
                    typedValue
                }
            }
        }

    override fun reset() {
        localValue = UninitializedValue
    }

    override fun isInitialized() = localValue !== UninitializedValue

    override fun toString() = if (isInitialized()) value.toString() else "Lazy(${groups.joinToString()}) value not initialized yet."
}

internal object UninitializedValue