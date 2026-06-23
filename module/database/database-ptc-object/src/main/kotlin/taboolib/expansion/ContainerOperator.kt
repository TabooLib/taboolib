package taboolib.expansion

import taboolib.module.database.*
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource

/**
 * ## 创建数据类
 * ```kotlin
 * // 数据类中必须存在
 * data class PlayerHome(
 *     @Id         // 主键（不是数据库意义上的主键，而是用于查询的主键）
 *     val username: UUID,
 *     @Key        // 建立索引（作用在于 update 时，若 @Id 重复出现，可以借助 @Key 来精确定位）
 *     @Length(32) // 数据在数据库中的长度
 *     val serverName: String,
 *     @Length(32)
 *     var world: String,
 *     var x: Double,
 *     var y: Double,
 *     var z: Double,
 *     var yaw: Float,
 *     var pitch: Float,
 *     var active: Boolean,
 * ) {
 *
 *     // 在数据类生成时，会优先查找解包函数，如果不存在则调用构造函数
 *     // 对于解包函数的要求：
 *     // 1. 名字任意
 *     // 2. 有且仅有一个 BundleMap 参数
 *     // 3. 返回值为该数据类
 *     companion object {
 *
 *         @JvmStatic
 *         fun wrap(map: BundleMap): PlayerFurniture {
 *             return PlayerHome(
 *                 map["username"],
 *                 map["server_name"], // serverName -> server_name
 *                 map["world"],
 *                 map["x"],
 *                 map["y"],
 *                 map["z"],
 *                 map["yaw"],
 *                 map["pitch"],
 *                 map["active"],
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * ## 创建容器
 * ```kotlin
 * val container = persistentContainer { new<PlayerHome>() } // PlayerHome -> player_home
 *
 * // 别忘记释放资源
 * fun close() {
 *     container.close()
 * }
 * ```
 *
 * ## 获取数据
 * ```kotlin
 * // 获取所有家
 * fun getPlayerHomes(username: UUID): List<PlayerHome> {
 *     return container.get<PlayerHome>().find(username)
 * }
 *
 * // 获取特定服务器中的家
 * fun getPlayerHomes(username: UUID, serverName: String): List<PlayerHome> {
 *     return container.get<PlayerHome>().find(username) {
 *         "server_name" eq serverName
 *     }
 * }
 * ```
 *
 * ## 更新数据
 * ```kotlin
 * // 借助 @Id 和 @Key 定位数据并更新
 * // 数据类中必须存在可变数据（var）才可执行 update 函数，因为该函数仅更新可变数据
 * fun updatePlayerHome(home: PlayerHome) {
 *     container.get<PlayerHome>().updateByKey(home)
 * }
 *
 * // 借助 @Id 和自定义条件定位数据并更新（不借助 @Key）
 * fun updatePlayerHome(home: PlayerHome, serverName: String) {
 *     container.get<PlayerHome>().update(home) {
 *         "server_name" eq serverName
 *     }
 * }
 * ```
 *
 * @author 坏黑
 * @since 2022/5/25 00:35
 */
abstract class ContainerOperator {

    abstract val table: Table<*, *>

    abstract val dataSource: DataSource

    /**
     * 获取数据，获取一个，有多个仅返回第一个（默认不经过任何条件判断）
     *
     * @param filter 条件过滤器
     */
    inline fun <reified T> getOne(noinline filter: Filter.() -> Unit = {}): T? {
        return getOne(T::class.java, filter)
    }

    /**
     * 获取数据，获取多个（默认不经过任何条件判断）
     *
     * @param filter 条件过滤器
     */
    inline fun <reified T> get(noinline filter: Filter.() -> Unit = {}): List<T> {
        return get(T::class.java, filter)
    }

    /**
     * 查询数据，查一个，有多个仅返回第一个
     *
     * @param id 数据类中的 @Id
     * @param filter 条件过滤器
     */
    inline fun <reified T> findOne(id: Any, noinline filter: Filter.() -> Unit = {}): T? {
        return findOne(T::class.java, id, filter)
    }

    /**
     * 查询数据，查多个
     *
     * @param id 数据类中的 @Id
     * @param filter 条件过滤器
     */
    inline fun <reified T> find(id: Any, noinline filter: Filter.() -> Unit = {}): List<T> {
        return find(T::class.java, id, filter)
    }

    /**
     * 正序排序
     *
     * @param row 排序的列
     * @param limit 限制返回的数量
     * @param filter 条件过滤器
     */
    inline fun <reified T> sort(row: String, limit: Int = 10, noinline filter: Filter.() -> Unit = {}): List<T> {
        return sort(T::class.java, row, limit, filter)
    }

    /**
     * 倒序排序
     *
     * @param row 排序的列
     * @param limit 限制返回的数量
     * @param filter 条件过滤器
     */
    inline fun <reified T> sortDescending(row: String, limit: Int = 10, noinline filter: Filter.() -> Unit = {}): List<T> {
        return sortDescending(T::class.java, row, limit, filter)
    }

    /**
     * 分页获取数据
     *
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     * @param filter 条件过滤器
     */
    inline fun <reified T> getPage(page: Int, size: Int, noinline filter: Filter.() -> Unit = {}): List<T> {
        return getPage(T::class.java, page, size, filter)
    }

    /**
     * 分页正序排序
     *
     * @param row 排序的列
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     * @param filter 条件过滤器
     */
    inline fun <reified T> sortPage(row: String, page: Int, size: Int, noinline filter: Filter.() -> Unit = {}): List<T> {
        return sortPage(T::class.java, row, page, size, filter)
    }

    /**
     * 分页倒序排序
     *
     * @param row 排序的列
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     * @param filter 条件过滤器
     */
    inline fun <reified T> sortDescendingPage(row: String, page: Int, size: Int, noinline filter: Filter.() -> Unit = {}): List<T> {
        return sortDescendingPage(T::class.java, row, page, size, filter)
    }

    /**
     * 游标查询，逐行读取数据，避免大量数据导致内存溢出。
     * 必须在事务中使用。
     *
     * @param filter 条件过滤器
     */
    inline fun <reified T> selectCursor(noinline filter: Filter.() -> Unit = {}): Cursor<T> {
        return selectCursor(T::class.java, filter)
    }

    /**
     * 游标查询（正序排序），逐行读取数据。
     * 必须在事务中使用。
     *
     * @param row 排序的列
     * @param filter 条件过滤器
     */
    inline fun <reified T> sortCursor(row: String, noinline filter: Filter.() -> Unit = {}): Cursor<T> {
        return sortCursor(T::class.java, row, filter)
    }

    /**
     * 游标查询（倒序排序），逐行读取数据。
     * 必须在事务中使用。
     *
     * @param row 排序的列
     * @param filter 条件过滤器
     */
    inline fun <reified T> sortDescendingCursor(row: String, noinline filter: Filter.() -> Unit = {}): Cursor<T> {
        return sortDescendingCursor(T::class.java, row, filter)
    }

    /**
     * 检查数据是否存在
     *
     * @param id 数据类中的 @Id
     * @param filter 条件过滤器
     */
    inline fun <reified T> has(id: Any, noinline filter: Filter.() -> Unit = {}): Boolean {
        return has(T::class.java, id, filter)
    }

    /**
     * 删除数据
     *
     * @param id 数据类中的 @Id
     * @param filter 条件过滤器
     */
    inline fun <reified T> delete(id: Any, noinline filter: Filter.() -> Unit = {}) {
        return delete(T::class.java, id, filter)
    }

    /**
     * 通过 @Id + @Key 查询数据，查多个
     *
     * 从数据对象中提取 @Id 和 @Key 字段值作为 WHERE 条件。
     *
     * @param data 数据对象（用于提取 @Id 和 @Key 值）
     * @param usePrimaryKey 是否使用 @Id 定位
     */
    inline fun <reified T> findByKey(data: Any, usePrimaryKey: Boolean = true): List<T> {
        return findByKey(T::class.java, data, usePrimaryKey)
    }

    /**
     * 通过 @Id + @Key 查询数据，查一个
     *
     * @param data 数据对象（用于提取 @Id 和 @Key 值）
     * @param usePrimaryKey 是否使用 @Id 定位
     */
    inline fun <reified T> findOneByKey(data: Any, usePrimaryKey: Boolean = true): T? {
        return findOneByKey(T::class.java, data, usePrimaryKey)
    }

    /**
     * 通过 @Id + @Key 检查数据是否存在
     *
     * @param data 数据对象（用于提取 @Id 和 @Key 值）
     * @param usePrimaryKey 是否使用 @Id 定位
     */
    inline fun <reified T> hasByKey(data: Any, usePrimaryKey: Boolean = true): Boolean {
        return hasByKey(T::class.java, data, usePrimaryKey)
    }

    /**
     * 通过自增行 ID 查询数据（用于无 @Id 字段的数据类）
     *
     * @param rowId 框架自动生成的 `id` 列的值
     */
    inline fun <reified T> findByRowId(rowId: Long): T? {
        return findByRowId(T::class.java, rowId)
    }

    /**
     * 批量查询，通过多个 @Id 值查询
     */
    inline fun <reified T> findByIds(ids: List<Any>): List<T> {
        return findByIds(T::class.java, ids)
    }

    /**
     * 批量删除，通过多个 @Id 值删除
     */
    inline fun <reified T> deleteByIds(ids: List<Any>) {
        return deleteByIds(T::class.java, ids)
    }

    /**
     * 获取数据，获取一个，有多个仅返回第一个（默认不经过任何条件判断）
     */
    abstract fun <T> getOne(type: Class<T>, filter: Filter.() -> Unit = {}): T?

    /**
     * 获取数据，获取多个（默认不经过任何条件判断）
     */
    abstract fun <T> get(type: Class<T>, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 查询数据，查一个，有多个仅返回第一个
     */
    abstract fun <T> findOne(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): T?

    /**
     * 查询数据，查多个
     */
    abstract fun <T> find(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 正序排序
     */
    abstract fun <T> sort(type: Class<T>, row: String, limit: Int = 10, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 倒序排序
     */
    abstract fun <T> sortDescending(type: Class<T>, row: String, limit: Int = 10, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 分页获取数据
     *
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     * @param filter 条件过滤器
     */
    abstract fun <T> getPage(type: Class<T>, page: Int, size: Int, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 分页正序排序
     *
     * @param row 排序的列
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     * @param filter 条件过滤器
     */
    abstract fun <T> sortPage(type: Class<T>, row: String, page: Int, size: Int, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 分页倒序排序
     *
     * @param row 排序的列
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     * @param filter 条件过滤器
     */
    abstract fun <T> sortDescendingPage(type: Class<T>, row: String, page: Int, size: Int, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 游标查询，逐行读取数据，避免大量数据导致内存溢出。
     * 必须在事务模式下调用（sharedConnection 不为 null）。
     *
     * @param filter 条件过滤器
     */
    abstract fun <T> selectCursor(type: Class<T>, filter: Filter.() -> Unit = {}): Cursor<T>

    /**
     * 游标查询（正序排序），逐行读取数据。
     * 必须在事务模式下调用。
     *
     * @param row 排序的列
     * @param filter 条件过滤器
     */
    abstract fun <T> sortCursor(type: Class<T>, row: String, filter: Filter.() -> Unit = {}): Cursor<T>

    /**
     * 游标查询（倒序排序），逐行读取数据。
     * 必须在事务模式下调用。
     *
     * @param row 排序的列
     * @param filter 条件过滤器
     */
    abstract fun <T> sortDescendingCursor(type: Class<T>, row: String, filter: Filter.() -> Unit = {}): Cursor<T>

    /**
     * 更新数据，借助 @Id 定位数据并更新
     *
     * @param data 数据类
     * @param usePrimaryKey 是否使用 @Id 定位数据，如果不使用则必须定义 filter 条件
     */
    abstract fun update(data: Any, usePrimaryKey: Boolean = true, filter: Filter.() -> Unit = {})

    /**
     * 更新数据，借助 @Id 和 @Key 定位数据并更新
     *
     * @param data 数据类
     * @param usePrimaryKey 是否使用 @Id 定位数据
     */
    abstract fun updateByKey(data: Any, usePrimaryKey: Boolean = true)

    /**
     * 批量更新或插入数据（upsert）
     *
     * 根据 @Id 和 @Key 判断数据是否存在：
     * - 存在则更新 var 字段
     * - 不存在则插入
     *
     * 在单个事务中执行，保证原子性和性能。
     *
     * @param dataList 数据列表
     */
    abstract fun upsert(dataList: List<Any>)

    /**
     * 插入数据
     */
    abstract fun insert(dataList: List<Any>)

    /**
     * 插入数据并返回自增主键
     *
     * @return 生成的自增主键列表
     */
    abstract fun insertAndGetKeys(dataList: List<Any>): List<Long>

    /**
     * 批量查询，通过多个 @Id 值查询（使用 IN 子句）
     *
     * @param ids @Id 字段值列表
     */
    abstract fun <T> findByIds(type: Class<T>, ids: List<Any>): List<T>

    /**
     * 批量删除，通过多个 @Id 值删除（使用 IN 子句）
     *
     * @param ids @Id 字段值列表
     */
    abstract fun <T> deleteByIds(type: Class<T>, ids: List<Any>)

    /**
     * 批量更新，通过 @Id + @Key 定位并更新 var 字段
     * 在单个事务中使用 batch PreparedStatement 执行，保证原子性和性能。
     *
     * @param dataList 数据列表
     */
    abstract fun updateBatch(dataList: List<Any>)

    /**
     * 检查数据
     */
    abstract fun <T> has(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): Boolean

    /**
     * 检查数据
     */
    abstract fun has(filter: Filter.() -> Unit): Boolean

    /**
     * 删除数据
     */
    abstract fun <T> delete(type: Class<T>, id: Any, filter: Filter.() -> Unit = {})

    /**
     * 按条件删除数据
     *
     * @param filter 条件过滤器
     */
    abstract fun deleteWhere(filter: Filter.() -> Unit)

    /**
     * 统计数据数量
     *
     * @param filter 条件过滤器
     */
    abstract fun count(filter: Filter.() -> Unit = {}): Long

    /**
     * 通过 @Id + @Key 查询数据，查多个
     *
     * 从数据对象中提取 @Id 和所有 @Key 字段值构建 WHERE 条件。
     */
    abstract fun <T> findByKey(type: Class<T>, data: Any, usePrimaryKey: Boolean = true): List<T>

    /**
     * 通过 @Id + @Key 查询数据，查一个，有多个仅返回第一个
     */
    abstract fun <T> findOneByKey(type: Class<T>, data: Any, usePrimaryKey: Boolean = true): T?

    /**
     * 通过 @Id + @Key 检查数据是否存在
     */
    abstract fun <T> hasByKey(type: Class<T>, data: Any, usePrimaryKey: Boolean = true): Boolean

    /**
     * 通过 @Id + @Key 删除数据
     *
     * @param data 数据对象（用于提取 @Id 和 @Key 值）
     * @param usePrimaryKey 是否使用 @Id 定位
     */
    abstract fun deleteByKey(data: Any, usePrimaryKey: Boolean = true)

    /**
     * 通过自增行 ID 查询数据（用于无 @Id 字段的数据类）
     *
     * 当数据类没有定义 @Id 字段时，框架会自动生成一个名为 `id` 的自增主键列。
     * 此方法通过该自增列的值查询数据。
     */
    abstract fun <T> findByRowId(type: Class<T>, rowId: Long): T?

    /**
     * 通过自增行 ID 删除数据（用于无 @Id 字段的数据类）
     */
    abstract fun deleteByRowId(rowId: Long)

    // === 自定义 SQL ===

    /**
     * 执行自定义 SELECT 查询
     *
     * @param action 查询动作
     * @param handler 结果集处理器
     */
    abstract fun <R> select(action: ActionSelect, handler: (ResultSet) -> R): R

    /**
     * 执行自定义更新操作（UPDATE / DELETE / INSERT）
     *
     * @param action 操作动作
     * @return 受影响的行数
     */
    abstract fun execute(action: Action): Int

    /**
     * 内部函数
     */
    protected fun Any.value(): Any {
        return when (this) {
            is IndexedEnum -> this.index
            is Enum<*> -> this.name
            is UUID -> if (table.host is HostPostgreSQL) this else this.toString()
            is Char -> this.code
            else -> CustomTypeFactory.getCustomType(this)?.serialize(this) ?: this
        }
    }
}

