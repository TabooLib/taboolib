package taboolib.common.util

import java.util.*

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

fun <T> resettableLazy(group: String = "*", synchronized: Boolean = false, initializer: () -> T): ResettableLazy<T> {
    return if (synchronized) {
        ResettableSynchronizedLazyImpl(group, initializer)
    } else {
        ResettableLazyImpl(group, initializer)
    }.also {
        ResettableLazy.defined += it
    }
}

abstract class ResettableLazy<T>(val group: String) : Lazy<T> {

    abstract fun reset()

    companion object {

        val defined = LinkedList<ResettableLazy<*>>()

        fun reset(group: String = "*") {
            synchronized(defined) { defined.filter { it.group == group }.forEach { it.reset() } }
        }
    }
}

private class ResettableLazyImpl<T>(group: String, initializer: () -> T) : ResettableLazy<T>(group) {

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

    override fun toString() = if (isInitialized()) value.toString() else "Lazy($group) value not initialized yet."
}

private class ResettableSynchronizedLazyImpl<T>(group: String, initializer: () -> T) : ResettableLazy<T>(group) {

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

    override fun toString() = if (isInitialized()) value.toString() else "Lazy($group) value not initialized yet."
}

internal object UninitializedValue