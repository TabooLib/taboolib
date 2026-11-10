package taboolib.expansion

import java.util.IdentityHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.sql.DataSource

class Database(val type: Type, val dataSource: DataSource = createOwnedDataSource(type)) : AutoCloseable {

    val ownsDataSource = takeOwnership(dataSource)

    private val closed = AtomicBoolean(false)

    constructor(type: Type, dataSource: DataSource, ownsDataSource: Boolean) : this(type, markOwnership(dataSource, ownsDataSource))

    init {
        try {
            type.tableVar().createTable(dataSource)
        } catch (ex: Throwable) {
            close()
            throw ex
        }
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

    /**
     * 关闭由当前实例创建的数据源。
     *
     * 外部传入的数据源默认由调用方管理，可通过三参数构造函数显式转移所有权。
     */
    override fun close() {
        if (!closed.compareAndSet(false, true) || !ownsDataSource) {
            return
        }
        (dataSource as? AutoCloseable)?.close()
    }

    companion object {

        private val ownedDataSources = ThreadLocal.withInitial { IdentityHashMap<DataSource, Unit>() }

        private fun createOwnedDataSource(type: Type): DataSource {
            return type.host().createDataSource().also {
                ownedDataSources.get()[it] = Unit
            }
        }

        private fun markOwnership(dataSource: DataSource, ownsDataSource: Boolean): DataSource {
            val ownership = ownedDataSources.get()
            if (ownsDataSource) {
                ownership[dataSource] = Unit
            } else {
                ownership.remove(dataSource)
            }
            if (ownership.isEmpty()) {
                ownedDataSources.remove()
            }
            return dataSource
        }

        private fun takeOwnership(dataSource: DataSource): Boolean {
            val ownership = ownedDataSources.get()
            val ownsDataSource = ownership.remove(dataSource) != null
            if (ownership.isEmpty()) {
                ownedDataSources.remove()
            }
            return ownsDataSource
        }
    }
}