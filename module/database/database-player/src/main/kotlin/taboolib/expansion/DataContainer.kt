package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submitAsync
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 缓存优先容器 Cache-First
 * 用于缓存数据并在一定时间后写入数据库
 * 数据库数据不同步给缓存
 *
 * @property user 用户标识
 * @property database 数据库实例
 */
class DataContainer(val user: String, val database: Database) {

    /** 存储用户数据的源 */
    val source = database[user]

    /** 存储需要更新的键值对及其更新时间 */
    val updateMap = ConcurrentHashMap<String, Long>()

    /**
     * 设置指定键的值并立即保存
     *
     * @param key 键
     * @param value 值
     */
    operator fun set(key: String, value: Any) {
        source[key] = value.toString()
        if (value.toString().isEmpty()) {
            source.remove(key)
            delete(key)
        } else {
            save(key)
        }
    }

    /**
     * 穿透缓存的写数据库方法
     *
     * @param targetUser 目标用户
     * @param key 键
     * @param value 值
     * @param sync 是否同步给内存，要求targetUser为UUID
     */
    fun forcedSet(targetUser: String, key: String, value: Any, sync: Boolean = false) {
        database[targetUser, key] = value.toString()
        // 因为 targetUser 不一定是UUID
        if (sync) {
            UUID.fromString(targetUser)?.let {
                playerDataContainer[it]?.source?.set(key, value.toString())
            }
        }
    }

    /**
     * 设置指定键的值，并在指定延迟后更新
     *
     * @param key 键
     * @param value 值
     * @param delay 延迟时间
     * @param timeUnit 时间单位
     */
    fun setDelayed(key: String, value: Any, delay: Long = 3L, timeUnit: TimeUnit = TimeUnit.SECONDS) {
        source[key] = value.toString()
        updateMap[key] = System.currentTimeMillis() - timeUnit.toMillis(delay)
    }

    /**
     * 获取指定键的值
     *
     * @param key 键
     * @return 对应的值，如果不存在则返回 null
     */
    operator fun get(key: String): String? {
        return source[key]
    }

    /**
     * 获取所有键的集合
     *
     * @return 键的集合
     */
    fun keys(): Set<String> {
        return source.keys
    }

    /**
     * 获取所有键值对
     *
     * @return 键值对映射
     */
    fun values(): Map<String, String> {
        return source
    }

    /**
     * 获取键值对的数量
     *
     * @return 键值对的数量
     */
    fun size(): Int {
        return source.size
    }

    /**
     * 保存指定键的值到数据库
     *
     * @param key 键
     */
    fun save(key: String) {
        submitAsync { database[user, key] = source[key]!! }
    }

    /**
     * 从数据库执行删除指定的键操作
     */
    fun delete(key: String) {
        submitAsync { database.remove(user, key) }
    }

    /**
     * 检查并更新需要保存的键值对
     */
    fun checkUpdate() {
        updateMap.filterValues { it < System.currentTimeMillis() }.forEach { (t, _) ->
            updateMap.remove(t)
            save(t)
        }
    }

    /**
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    override fun toString(): String {
        return "DataContainer(user='$user', source=$source)"
    }

    /**
     * 内部伴生对象，用于定期检查更新
     */
    @Inject
    internal companion object {

        /**
         * 定期检查并更新所有 DataContainer 实例
         */
        @Schedule(period = 20)
        fun checkUpdate() {
            playerDataContainer.entries.forEach { it.value.checkUpdate() }
        }
    }
}
