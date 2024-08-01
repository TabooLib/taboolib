package taboolib.expansion

// 只读数据容器
class ReadOnlyDataContainer(val user: String, val database: Database) {

    val source = database[user]

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
}
