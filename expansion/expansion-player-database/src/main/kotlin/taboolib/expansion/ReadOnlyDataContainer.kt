package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submitAsync

// 只读数据容器
class ReadOnlyDataContainer(val user: String, val database: Database) {

    var source = database[user]

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

    override fun toString(): String {
        return "ReadOnlyDataContainer(user='$user', source=$source)"
    }

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
                playerReadOnlyDataContainer.values.forEach {
                    runCatching { it.update() }
                }
            }
        }
    }
}
