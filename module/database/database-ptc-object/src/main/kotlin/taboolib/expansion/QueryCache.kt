package taboolib.expansion

import java.util.concurrent.ConcurrentHashMap

/**
 * 数据缓存接口
 *
 * 用户可实现此接口来自定义缓存策略（如 Redis、Caffeine 等）。
 *
 * ```kotlin
 * // 使用自定义缓存
 * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
 *     cache(MyCaffeineCache())
 * }
 *
 * // 自定义实现示例
 * class MyCaffeineCache : DataCache {
 *     private val caffeine = Caffeine.newBuilder().maximumSize(1000).build<String, Any?>()
 *     override fun get(key: String, loader: () -> Any?) = caffeine.get(key) { loader() }
 *     override fun invalidate(key: String) = caffeine.invalidate(key)
 *     override fun invalidateByPrefix(prefix: String) {
 *         caffeine.asMap().keys.removeIf { it.startsWith(prefix) }
 *     }
 *     override fun invalidateAll() = caffeine.invalidateAll()
 * }
 * ```
 *
 * ### 缓存键格式
 *
 * DataMapper 自动构建缓存键，格式为 `"方法名:参数1,参数2,..."`，例如：
 * - `findById:550e8400-e29b-41d4-a716-446655440000,...`
 * - `findAll:...`
 * - `count:...`
 *
 * ### 失效策略
 *
 * DataMapper 在写入操作后会调用以下方法进行缓存失效：
 * - **单条写入**（update/deleteById 等）：调用 [invalidateByPrefix] 精确失效受影响的 ID 缓存 + 集合缓存
 * - **批量/不确定范围写入**（deleteWhere/rawUpdate 等）：调用 [invalidateAll] 全量失效
 */
interface DataCache {

    /**
     * 从缓存中获取值，未命中时调用 loader 加载并写入缓存
     *
     * @param key 缓存键（由方法名+参数自动构建）
     * @param loader 缓存未命中时的加载函数
     * @return 缓存值或 loader 返回值
     */
    fun get(key: String, loader: () -> Any?): Any?

    /**
     * 使指定 key 的缓存失效
     */
    fun invalidate(key: String)

    /**
     * 使所有以指定前缀开头的缓存失效
     *
     * 例如 `invalidateByPrefix("findAll:")` 会清除所有 `findAll:*` 缓存
     */
    fun invalidateByPrefix(prefix: String)

    /**
     * 清除所有缓存条目
     */
    fun invalidateAll()
}

/**
 * 内置缓存配置
 */
class QueryCacheConfig {
    var maximumSize: Int = 256
    var expireAfterWrite: Long = 0   // 秒，0 = 不过期
    var expireAfterAccess: Long = 0  // 秒，0 = 不过期
}

/**
 * 基于 ConcurrentHashMap 的内置缓存实现
 *
 * - 查询时：以方法名+参数构建 key，命中则返回缓存值
 * - 写入时：按前缀或精确 key 失效受影响的缓存
 * - 支持 TTL 过期和最大容量限制
 */
class QueryCache(private val config: QueryCacheConfig) : DataCache {

    private data class CacheEntry(val value: Any?, val writeTime: Long, var accessTime: Long)

    private val store = ConcurrentHashMap<String, CacheEntry>()

    override fun get(key: String, loader: () -> Any?): Any? {
        val entry = store[key]
        if (entry != null && !isExpired(entry)) {
            entry.accessTime = System.currentTimeMillis()
            return entry.value
        }
        val value = loader()
        put(key, value)
        return value
    }

    override fun invalidate(key: String) {
        store.remove(key)
    }

    override fun invalidateByPrefix(prefix: String) {
        store.keys.removeIf { it.startsWith(prefix) }
    }

    override fun invalidateAll() {
        store.clear()
    }

    private fun put(key: String, value: Any?) {
        evictIfNeeded()
        val now = System.currentTimeMillis()
        store[key] = CacheEntry(value, now, now)
    }

    private fun isExpired(entry: CacheEntry): Boolean {
        val now = System.currentTimeMillis()
        if (config.expireAfterWrite > 0 && now - entry.writeTime > config.expireAfterWrite * 1000) return true
        if (config.expireAfterAccess > 0 && now - entry.accessTime > config.expireAfterAccess * 1000) return true
        return false
    }

    private fun evictIfNeeded() {
        // 先清除过期条目
        store.entries.removeIf { isExpired(it.value) }
        // 超出容量则移除最旧的
        while (store.size >= config.maximumSize) {
            val oldest = store.entries.minByOrNull { it.value.accessTime } ?: break
            store.remove(oldest.key)
        }
    }
}