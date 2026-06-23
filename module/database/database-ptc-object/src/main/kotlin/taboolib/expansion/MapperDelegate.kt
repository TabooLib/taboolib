package taboolib.expansion

import taboolib.expansion.mapper.DataMapperImpl
import taboolib.expansion.migration.MigrationConfig
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Mapper 配置
 */
class MapperConfig<T> {
    internal var cacheInstance: L2Cache? = null
    internal var manualTableSQL: List<String>? = null
    internal var migrationInstance: MigrationConfig? = null

    /**
     * 手动建表：跳过自动建表，执行用户提供的 SQL
     *
     * ```kotlin
     * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
     *     manualTable(
     *         """CREATE TABLE IF NOT EXISTS player_home (
     *             username VARCHAR(64) PRIMARY KEY,
     *             world VARCHAR(64)
     *         )"""
     *     )
     * }
     * ```
     */
    fun manualTable(vararg sql: String) {
        manualTableSQL = sql.toList()
    }

    /**
     * 版本迁移：通过 meta 表跟踪版本号，按版本执行迁移 SQL
     *
     * ```kotlin
     * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
     *     migration {
     *         version(1,
     *             "ALTER TABLE player_home ADD COLUMN x DOUBLE DEFAULT 0",
     *             "ALTER TABLE player_home ADD COLUMN y DOUBLE DEFAULT 0"
     *         )
     *         version(2,
     *             "ALTER TABLE player_home ADD COLUMN z DOUBLE DEFAULT 0"
     *         )
     *     }
     * }
     * ```
     */
    fun migration(block: MigrationConfig.() -> Unit) {
        migrationInstance = MigrationConfig().apply(block)
    }

    /**
     * 使用内置 L2 双层缓存
     *
     * ```kotlin
     * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
     *     cache {
     *         beanCache { maximumSize = 10000; expireAfterAccess = 600 }
     *         queryCache { maximumSize = 1000; expireAfterAccess = 600 }
     *     }
     * }
     * ```
     */
    fun cache(block: L2CacheConfig.() -> Unit) {
        cacheInstance = L2Cache(L2CacheConfig().apply(block))
    }

    /**
     * 使用自定义 DataCache 实现构建 L2 缓存
     *
     * ```kotlin
     * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
     *     cache(MyCaffeineCache(), MyCaffeineCache())
     * }
     * ```
     */
    fun cache(beanCache: DataCache, queryCache: DataCache) {
        cacheInstance = L2Cache(beanCache, queryCache)
    }

    /**
     * 使用同一个自定义 DataCache 实现作为 Bean Cache 和 Query Cache
     *
     * 适用于用户自行实现 Redis、Caffeine 等缓存后端的场景。
     *
     * ```kotlin
     * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
     *     cache(MyRedisCache())
     * }
     * ```
     */
    fun cache(cache: DataCache) {
        cacheInstance = L2Cache(cache, cache)
    }
}

/**
 * 创建 DataMapper 属性委托
 *
 * 缓存默认关闭，需要显式调用 `cache {}` 开启。
 *
 * ```kotlin
 * // 不带缓存（默认）
 * val homeTable by mapper<PlayerHome>(dbFile("data.db"))
 *
 * // 使用内置 L2 缓存
 * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
 *     cache {
 *         beanCache { maximumSize = 10000; expireAfterAccess = 600 }
 *         queryCache { maximumSize = 1000; expireAfterAccess = 600 }
 *     }
 * }
 *
 * // 使用自定义缓存
 * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
 *     cache(MyCaffeineCache(), MyCaffeineCache())
 * }
 *
 * // 使用同一个自定义缓存实现
 * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
 *     cache(MyRedisCache())
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
            container.manualTableStatements = mapperConfig.manualTableSQL
            container.migrationInstance = mapperConfig.migrationInstance
            new(type)
        }
        return DataMapperImpl(type, container, mapperConfig.cacheInstance)
    }
}
