package taboolib.expansion

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Mapper 配置
 */
class MapperConfig<T> {
    internal var cacheInstance: DataCache? = null

    /**
     * 使用内置 ConcurrentHashMap 缓存
     *
     * ```kotlin
     * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
     *     cache {
     *         maximumSize = 1000
     *         expireAfterWrite = 300
     *     }
     * }
     * ```
     */
    fun cache(block: QueryCacheConfig.() -> Unit) {
        cacheInstance = QueryCache(QueryCacheConfig().apply(block))
    }

    /**
     * 使用自定义缓存实现
     *
     * ```kotlin
     * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
     *     cache(MyCaffeineCache())
     * }
     * ```
     */
    fun cache(cache: DataCache) {
        cacheInstance = cache
    }
}

/**
 * 创建 DataMapper 属性委托
 *
 * ```kotlin
 * val homeTable by mapper<PlayerHome>(dbFile("data.db"))
 *
 * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
 *     cache {
 *         enabled = true
 *         maximumSize = 1000
 *     }
 * }
 * ```
 */
inline fun <reified T> mapper(
    source: Any = db(),
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    noinline config: MapperConfig<T>.() -> Unit = {}
): ReadOnlyProperty<Any?, DataMapper<T>> {
    return MapperDelegate(T::class.java, source, flags, clearFlags, ssl, config)
}

/**
 * 属性委托实现，懒加载创建容器和 DataMapper
 */
class MapperDelegate<T>(
    private val type: Class<T>,
    private val source: Any,
    private val flags: List<String>,
    private val clearFlags: Boolean,
    private val ssl: String?,
    private val config: MapperConfig<T>.() -> Unit
) : ReadOnlyProperty<Any?, DataMapper<T>> {

    @Volatile
    private var instance: DataMapper<T>? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): DataMapper<T> {
        return instance ?: synchronized(this) {
            instance ?: createMapper().also { instance = it }
        }
    }

    private fun createMapper(): DataMapper<T> {
        val mapperConfig = MapperConfig<T>().apply(config)
        val container = persistentContainer(source, flags, clearFlags, ssl) {
            new(type)
        }
        return DataMapperImpl(type, container, mapperConfig.cacheInstance)
    }
}
