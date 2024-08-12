package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submitAsync

/**
 *  数据库优先容器 Database-First
 *  定期从数据库取出数据同步给缓存
 */
class AutoDataContainer(val user: String, val database: Database) {

    var source = database[user]

    operator fun get(key: String): String? {
        return source[key]
    }

    /**
     *  设置操作是穿透缓存直接写数据库，然后进行缓存同步
     *  这里只操作单个 key 的缓存 是因为防止高频率set导致频繁读数据库
     */
    operator fun set(key: String, value: Any) {
        database[user, key] = value.toString()
        source[key] = value.toString()
    }

    fun keys(): Set<String> {
        return source.keys
    }

    fun values(): Map<String, String> {
        return source
    }

    fun size(): Int {
        return source.size
    }

    override fun toString(): String {
        return "AutoDataContainer(user='$user', source=$source)"
    }

    /**
     *  手动更新数据 可以酌情使用
     */
    fun update() {
        source = database[user]
    }


    @Inject
    internal companion object {

        // 延迟更新时间
        var syncTick = 80L

        @Awake(LifeCycle.ACTIVE)
        private fun update() {
            submitAsync(period = syncTick) {
                playerAutoDataContainer.entries.forEach {
                    runCatching { it.value.update() }
                }
            }
        }
    }
}
