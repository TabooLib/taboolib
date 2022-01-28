package taboolib.common.io

import org.tabooproject.reflex.FastInstGetter
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.common.io.LazyInstGetter
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
@Suppress("UNCHECKED_CAST")
class InstGetterLazy<T> private constructor(source: Class<T>, private val newInstance: Boolean = false) : InstGetter<T>(source) {

    val inst by lazy {
        FastInstGetter(source.name)
    }

    val instance by lazy {
        runCatching { inst.instance as T }.getOrElse { runCatching { inst.companion as T }.getOrElse { if (newInstance) source.invokeConstructor() else null } }
    }

    override fun get(): T? {
        return instance
    }

    companion object {

        val getterMap = ConcurrentHashMap<String, InstGetterLazy<*>>()

        fun <T> of(source: Class<T>, newInstance: Boolean = false): InstGetterLazy<T> {
            return getterMap.computeIfAbsent(source.name) { InstGetterLazy(source, newInstance) } as InstGetterLazy<T>
        }
    }
}