package taboolib.expansion

import taboolib.module.database.Table
import taboolib.module.database.Filter
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
    inline fun <reified T> getOne(noinline filter: Filter.() -> Unit = {}): T? {
        return getOne(T::class.java, filter)
    }

    /** 查询数据 */
    inline fun <reified T> get(noinline filter: Filter.() -> Unit = {}): List<T> {
        return get(T::class.java, filter)
    }

    /** 查询数据 */
    inline fun <reified T> findOne(id: Any, noinline filter: Filter.() -> Unit = {}): T? {
        return findOne(T::class.java, id, filter)
    }

    /** 查询数据 */
    inline fun <reified T> find(id: Any, noinline filter: Filter.() -> Unit = {}): List<T> {
        return find(T::class.java, id, filter)
    }

    /** 正序排序 */
    inline fun <reified T> sort(row: String, limit: Int = 10, noinline filter: Filter.() -> Unit = {}): List<T> {
        return sort(T::class.java, row, limit, filter)
    }

    /** 倒序排序 */
    inline fun <reified T> sortDescending(row: String, limit: Int = 10, noinline filter: Filter.() -> Unit = {}): List<T> {
        return sortDescending(T::class.java, row, limit, filter)
    }

    /** 检查数据 */
    inline fun <reified T> has(id: Any, noinline filter: Filter.() -> Unit = {}): Boolean {
        return has(T::class.java, id, filter)
    }

    /** 删除数据 */
    inline fun <reified T> delete(id: Any, noinline filter: Filter.() -> Unit = {}) {
        return delete(T::class.java, id, filter)
    }

    /** 查询数据 */
    abstract fun <T> getOne(type: Class<T>, filter: Filter.() -> Unit = {}): T?

    /** 查询数据 */
    abstract fun <T> get(type: Class<T>, filter: Filter.() -> Unit = {}): List<T>

    /** 查询数据 */
    abstract fun <T> findOne(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): T?

    /** 查询数据 */
    abstract fun <T> find(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): List<T>

    /** 正序排序 */
    abstract fun <T> sort(type: Class<T>, row: String, limit: Int = 10, filter: Filter.() -> Unit = {}): List<T>

    /** 倒序排序 */
    abstract fun <T> sortDescending(type: Class<T>, row: String, limit: Int = 10, filter: Filter.() -> Unit = {}): List<T>

    /** 更新数据 */
    abstract fun update(data: Any, filter: Filter.() -> Unit = {})

    /** 更新数据 */
    abstract fun updateByKey(data: Any)

    /** 插入数据 */
    abstract fun insert(dataList: List<Any>)

    /** 检查数据 */
    abstract fun <T> has(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): Boolean

    /** 检查数据 */
    abstract fun has(filter: Filter.() -> Unit): Boolean

    /** 删除数据 */
    abstract fun <T> delete(type: Class<T>, id: Any, filter: Filter.() -> Unit = {})

    protected fun Any.value(): Any {
        return when (this) {
            is UUID -> this.toString()
            is Char -> this.code
            else -> CustomTypeFactory.getCustomType(this)?.serialize(this) ?: this
        }
    }
}

