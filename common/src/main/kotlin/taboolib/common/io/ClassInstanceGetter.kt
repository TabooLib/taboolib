package taboolib.common.io

import org.tabooproject.reflex.FastInstGetter
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import java.util.concurrent.ConcurrentHashMap

abstract class InstGetter<T>(private val source: Class<T>) {

    abstract fun get(): T?
}

/**
 * 已有实例无需获取的类，常见于 @Awake 自唤醒类
 */
class InstantInstGetter<T>(source: Class<T>, val instance: T) : InstGetter<T>(source) {

    override fun get(): T? {
        return instance
    }
}

/**
 * 错误的类
 */
class ErrorInstGetter<T>(source: Class<T>, val throwable: Throwable) : InstGetter<T>(source) {

    init {
        IllegalStateException("Exception getting an instance of $source", throwable).printStackTrace()
    }

    override fun get(): T? {
        return null
    }
}

/**
 * 借助 FastInstGetter 获取实例
 */
class LazyInstGetter<T> private constructor(source: Class<T>, private val newInstance: Boolean = false) : InstGetter<T>(source) {

    private val inst by lazy {
        FastInstGetter(source.name)
    }

    @Suppress("UNCHECKED_CAST")
    private val instance by lazy {
        try {
            inst.instance as T
        } catch (_: NoSuchFieldError) {
            try {
                inst.companion as T
            } catch (_: NoSuchFieldError) {
                if (newInstance) source.invokeConstructor() else null
            }
        }
    }

    override fun get(): T? {
        return instance
    }

    companion object {

        private val getterMap = ConcurrentHashMap<String, LazyInstGetter<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <T> of(source: Class<T>, newInstance: Boolean = false): LazyInstGetter<T> {
            return getterMap.computeIfAbsent(source.name) { LazyInstGetter(source, newInstance) } as LazyInstGetter<T>
        }
    }
}