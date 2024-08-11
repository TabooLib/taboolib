package taboolib.expansion

import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

class Database(val type: Type, val dataSource: DataSource = type.host().createDataSource()) {

    init {
        type.tableVar().createTable(dataSource)
    }

    operator fun get(user: String): MutableMap<String, String> {
        return type.tableVar().select(dataSource) {
            rows("key", "value")
            where("user" eq user)
        }.map {
            getString("key") to getString("value")
        }.toMap(ConcurrentHashMap())
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
        if (data.isEmpty()) {
            remove(user, name)
            return
        }
        if (get(user, name) == null) {
            type.tableVar().insert(dataSource, "user", "key", "value") { value(user, name, data) }
        } else {
            type.tableVar().update(dataSource) {
                set("value", data)
                where("user" eq user and ("key" eq name))
            }
        }
    }

    // 查询某个User的Key对应的Value
    fun select(user: String, key: String): String? {
        return type.tableVar().select(dataSource) {
            rows("value")
            where("user" eq user and ("key" eq key))
        }.firstOrNull {
            getString("value")
        }
    }

    // 根据Key获取所有数据
    // return: Map<user, value>
    fun getList(name: String): MutableMap<String, String> {
        return type.tableVar().select(dataSource) {
            rows("user", "value")
            where("key" eq name)
        }.map {
            getString("user") to getString("value")
        }.toMap(ConcurrentHashMap())
    }

    fun getList(name: String, value: String): List<String> {
        return type.tableVar().select(dataSource) {
            rows("user")
            where("key" eq name and ("value" eq value))
        }.map {
            getString("user")
        }
    }

    fun remove(user: String, name: String) {
        type.tableVar().delete(dataSource) {
            where("user" eq user and ("key" eq name))
        }
    }


}
