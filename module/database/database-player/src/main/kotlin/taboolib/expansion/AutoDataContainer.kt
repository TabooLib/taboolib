package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submitAsync

/**
 * 数据库优先容器，定期从数据库取出数据同步给缓存。
 *
 * @property user 用户标识
 * @property database 数据库实例
 */
class AutoDataContainer(val user: String, val database: Database) {

    /**
     * 存储用户数据的源
     */
    var source = database[user]

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
     * 设置指定键的值
     *
     * 设置操作是穿透缓存直接写数据库，然后进行缓存同步。
     * 这里只操作单个 key 的缓存，是因为防止高频率 set 导致频繁读数据库。
     *
     * @param key 键
     * @param value 值
     */
    operator fun set(key: String, value: Any) {
        val save = value.toString()
        database[user, key] = save
        if (save.isEmpty()) {
            source.remove(key)
        } else {
            source[key] = save
        }
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
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    override fun toString(): String {
        return "AutoDataContainer(user='$user', source=$source)"
    }

    /**
     * 手动更新数据
     *
     * 可以酌情使用
     */
    fun update() {
        source = database[user]
    }

    @Inject
    internal companion object {

        /**
         * 延迟更新时间
         */
        var syncTick = 80L

        /**
         * 定期更新所有 AutoDataContainer 实例的数据
         */
        @Awake(LifeCycle.ACTIVE)
        fun update() {
            submitAsync(period = syncTick) {
                playerAutoDataContainer.entries.forEach {
                    runCatching { it.value.update() }
                }
            }
        }
    }
}
