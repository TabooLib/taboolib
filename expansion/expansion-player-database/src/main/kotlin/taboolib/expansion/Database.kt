package taboolib.expansion

class Database(val type: Type) {

    val dataSource = type.host().createDataSource()

    init {
        type.tableVar().createTable(dataSource)
    }

    operator fun get(user: String): MutableMap<String, String> {
        return type.tableVar().select(dataSource) {
            rows("key", "value")
            where("user" eq user)
        }.map {
            getString("key") to getString("value")
        }.toMap(HashMap())
    }

    operator fun get(user: String, name: String): String? {
        return type.tableVar().select(dataSource) {
            rows("value")
            where("user" eq user and ("key" eq name))
            limit(1)
        }.firstOrNull {
            getString("value")
        }
    }

    operator fun set(user: String, name: String, data: String) {
        if (get(user, name) == null) {
            type.tableVar().insert(dataSource, "user", "key", "value") { value(user, name, data) }
        } else {
            type.tableVar().update(dataSource) {
                set("value", data)
                where("user" eq user and ("key" eq name))
            }
        }
    }
}