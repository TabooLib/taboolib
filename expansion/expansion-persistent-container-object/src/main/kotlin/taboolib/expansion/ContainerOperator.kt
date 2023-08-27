package taboolib.expansion

import taboolib.module.database.Table
import taboolib.module.database.Where
import java.util.*
import javax.sql.DataSource

/**
 * @author 坏黑
 * @since 2022/5/25 00:35
 */
abstract class ContainerOperator {

    abstract val table: Table<*, *>

    abstract val dataSource: DataSource

    /** 查询数据 */
    inline fun <reified T> getOne(noinline where: Where.() -> Unit = {}): T? {
        return getOne(T::class.java, where)
    }

    /** 查询数据 */
    inline fun <reified T> get(noinline where: Where.() -> Unit = {}): List<T> {
        return get(T::class.java, where)
    }

    /** 查询数据 */
    inline fun <reified T> findOne(id: Any, noinline where: Where.() -> Unit = {}): T? {
        return findOne(T::class.java, id, where)
    }

    /** 查询数据 */
    inline fun <reified T> find(id: Any, noinline where: Where.() -> Unit = {}): List<T> {
        return find(T::class.java, id, where)
    }

    /** 正序排序 */
    inline fun <reified T> sort(row: String, limit: Int = 10, noinline where: Where.() -> Unit = {}): List<T> {
        return sort(T::class.java, row, limit, where)
    }

    /** 倒序排序 */
    inline fun <reified T> sortDescending(row: String, limit: Int = 10, noinline where: Where.() -> Unit = {}): List<T> {
        return sortDescending(T::class.java, row, limit, where)
    }

    /** 检查数据 */
    inline fun <reified T> has(id: Any, noinline where: Where.() -> Unit = {}): Boolean {
        return has(T::class.java, id, where)
    }

    /** 删除数据 */
    inline fun <reified T> delete(id: Any, noinline where: Where.() -> Unit = {}) {
        return delete(T::class.java, id, where)
    }

    /** 查询数据 */
    abstract fun <T> getOne(type: Class<T>, where: Where.() -> Unit = {}): T?

    /** 查询数据 */
    abstract fun <T> get(type: Class<T>, where: Where.() -> Unit = {}): List<T>

    /** 查询数据 */
    abstract fun <T> findOne(type: Class<T>, id: Any, where: Where.() -> Unit = {}): T?

    /** 查询数据 */
    abstract fun <T> find(type: Class<T>, id: Any, where: Where.() -> Unit = {}): List<T>

    /** 正序排序 */
    abstract fun <T> sort(type: Class<T>, row: String, limit: Int = 10, where: Where.() -> Unit = {}): List<T>

    /** 倒序排序 */
    abstract fun <T> sortDescending(type: Class<T>, row: String, limit: Int = 10, where: Where.() -> Unit = {}): List<T>

    /** 更新数据 */
    abstract fun update(data: Any, where: Where.() -> Unit = {})

    /** 更新数据 */
    abstract fun updateByKey(data: Any)

    /** 插入数据 */
    abstract fun insert(dataList: List<Any>)

    /** 检查数据 */
    abstract fun <T> has(type: Class<T>, id: Any, where: Where.() -> Unit = {}): Boolean

    /** 检查数据 */
    abstract fun has(where: Where.() -> Unit): Boolean

    /** 删除数据 */
    abstract fun <T> delete(type: Class<T>, id: Any, where: Where.() -> Unit = {})

    protected fun Any.value(): Any {
        return when (this) {
            is UUID -> this.toString()
            is Char -> this.code
            else -> CustomTypeFactory.getCustomType(this)?.serialize(this) ?: this
        }
    }
}

