package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submitAsync
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.module.Vars
 *
 * @author sky
 * @since 2021/8/22 7:51 下午
 */
class DataContainer(val user: String, val database: Database) {

    val source = database[user]
    val updateMap = ConcurrentHashMap<String, Long>()

    operator fun set(key: String, value: Any) {
        source[key] = value.toString()
        save(key)
    }

    // 穿透缓存的写数据库方法
    operator fun set(targetUser: String, key: String, value: Any) {
        database[targetUser, key] = value.toString()
    }

    fun setDelayed(key: String, value: Any, delay: Long = 3L, timeUnit: TimeUnit = TimeUnit.SECONDS) {
        source[key] = value.toString()
        updateMap[key] = System.currentTimeMillis() - timeUnit.toMillis(delay)
    }

    operator fun get(key: String): String? {
        return source[key]
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

    fun save(key: String) {
        submitAsync { database[user, key] = source[key]!! }
    }

    fun checkUpdate() {
        updateMap.filterValues { it < System.currentTimeMillis() }.forEach { (t, _) ->
            updateMap.remove(t)
            save(t)
        }
    }

    override fun toString(): String {
        return "DataContainer(user='$user', source=$source)"
    }

    @Inject
    internal companion object {

        @Schedule(period = 20)
        private fun checkUpdate() {
            playerDataContainer.values.forEach { it.checkUpdate() }
        }
    }
}
