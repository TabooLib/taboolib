package taboolib.expansion

import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

class Database(val type: Type, val dataSource: DataSource = type.host().createDataSource()) {

    init {
        type.tableVar().createTable(dataSource)
    }

    /**
     *  根据用户获取用户所有的数据
     */
    operator fun get(user: String): MutableMap<String, String> {
        return type.tableVar().select(dataSource) {
            rows("key", "value")
            where("user" eq user)
        }.map {
            getString("key") to getString("value")
        }.toMap(ConcurrentHashMap())
    }

    /**
     *  根据用户和键获取数据
     */
    operator fun get(user: String, key: String): String? {
        return type.tableVar().select(dataSource) {
            rows("value")
            where("user" eq user and ("key" eq key))
            limit(1)
        }.firstOrNull {
            getString("value")
        }
    }

    /**
     *  设置用户数据
     *  如果数据为空则转为删除操作
     */
    operator fun set(user: String, key: String, data: String) {
        if (data.isEmpty()) {
            remove(user, key)
            return
        }
        if (get(user, key) == null) {
            type.tableVar().insert(dataSource, "user", "key", "value") {
                value(user, key, data)
            }
        } else {
            type.tableVar().update(dataSource) {
                set("value", data)
                where("user" eq user and ("key" eq key))
            }
        }
    }

    /**
     *  查询数据 根据 用户名 与 键
     *  如果数据不存在则返回 null
     */
    fun getValue(user: String, key: String): String? {
        return type.tableVar().select(dataSource) {
            rows("key", "value")
            where("user" eq user and ("key" eq key))
        }.firstOrNull {
            getString("value")
        }
    }

    /**
     *  返回所有满足 Key = Value 的用户
     */
    fun getUserList(key: String, value: String): List<String> {
        return type.tableVar().select(dataSource) {
            rows("user")
            where("key" eq key and ("value" eq value))
        }.map {
            getString("user")
        }
    }

    /**
     *  根据 Key 来返回一个 <User,Value> 的Map
     */
    fun getListByKey(key: String): MutableMap<String, String> {
        return type.tableVar().select(dataSource) {
            rows("user", "value")
            where("key" eq key)
        }.map {
            getString("user") to getString("value")
        }.toMap(ConcurrentHashMap())
    }

    /**
     *  根据一个 Key 来尾缀模糊查询User的相关数据
     *  例如 key = "title-" 则会查询所有以 "title-" 开头的数据
     */
    fun getLikeKeyList(user: String, key: String): MutableMap<String, String> {
        return type.tableVar().select(dataSource) {
            rows("key", "value")
            where("user" eq user and ("key" like "${key}%"))
        }.map {
            getString("key") to getString("value")
        }.toMap(ConcurrentHashMap())
    }

    /**
     *  删除符合条件的数据
     */
    fun remove(user: String, key: String) {
        type.tableVar().delete(dataSource) {
            where("user" eq user and ("key" eq key))
        }
    }
}