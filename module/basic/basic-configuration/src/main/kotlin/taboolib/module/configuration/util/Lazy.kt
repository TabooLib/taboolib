package taboolib.module.configuration.util

import taboolib.module.configuration.Configuration
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 一个“按 Key 失效”的懒加载委托。
 *
 * - 首次访问时执行 [initializer] 并缓存结果
 * - 之后每次访问都会计算 [key]，当 Key 发生变化时会重新执行 [initializer]
 * - 线程安全：使用双重检查 + `synchronized` 保护初始化与更新
 */
class KeyedLazy<T>(private val key: () -> Any?, private val initializer: () -> T) : ReadOnlyProperty<Any?, T> {

    private val lock = Any()

    @Volatile
    private var state: State<T>? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val currentKey = key()
        val current = state
        if (current != null && current.key == currentKey) {
            return current.value
        }

        return synchronized(lock) {
            val latestKey = key()
            val latest = state
            if (latest != null && latest.key == latestKey) {
                return@synchronized latest.value
            }

            val newValue = initializer()
            state = State(latestKey, newValue)
            newValue
        }
    }

    private data class State<T>(val key: Any?, val value: T)
}

/**
 * 一个“随配置重载代数失效”的懒加载委托。
 *
 * - 以 [Configuration.reloadGeneration]（或其他可变 Key）作为失效条件
 * - 当代数变化时重新执行 [initializer] 并更新缓存
 * - 线程安全：使用双重检查 + `synchronized` 保护初始化与更新
 *
 * `config` 的用法：
 * - 传入 [Configuration]：使用其 [Configuration.reloadGeneration]
 * - 传入 `File`：使用 `lastModified XOR length` 作为指纹
 * - 传入 `() -> Any?`：使用返回值作为指纹（推荐，最灵活）
 * - 传入其他对象：使用 `hashCode()` 作为指纹（通常不会随内容变化）
 * - 传入 `null`：若 `thisRef` 是 [Configuration]，则使用 `thisRef.reloadGeneration`；否则将不会自动失效
 */
class ReloadAwareLazy<T>(private val config: Any? = null, private val initializer: () -> T) : ReadOnlyProperty<Any?, T> {

    private val lock = Any()

    @Volatile
    private var state: State<T>? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val currentKey = resolveKey(thisRef)
        val current = state
        if (current != null && current.key == currentKey) {
            return current.value
        }

        return synchronized(lock) {
            val latestKey = resolveKey(thisRef)
            val latest = state
            if (latest != null && latest.key == latestKey) {
                return@synchronized latest.value
            }

            val newValue = initializer()
            state = State(latestKey, newValue)
            newValue
        }
    }

    private fun resolveKey(thisRef: Any?): Any? {
        return when (val c = config) {
            is Configuration -> c.reloadGeneration
            is java.io.File -> FileFingerprint(c.lastModified(), c.length())
            is Function0<*> -> c.invoke()
            null -> (thisRef as? Configuration)?.reloadGeneration ?: 0
            else -> c.hashCode()
        }
    }

    private data class FileFingerprint(val lastModified: Long, val length: Long)

    private data class State<T>(val key: Any?, val value: T)
}
