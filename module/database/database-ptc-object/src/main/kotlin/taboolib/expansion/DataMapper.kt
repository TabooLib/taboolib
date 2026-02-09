package taboolib.expansion

import taboolib.module.database.Action
import taboolib.module.database.ActionDelete
import taboolib.module.database.ActionSelect
import taboolib.module.database.ActionUpdate
import taboolib.module.database.Filter
import java.sql.ResultSet

/**
 * 数据映射器接口，提供类型安全的 CRUD 操作入口。
 *
 * 通过 Kotlin by 委托使用：
 * ```
 * val homeTable by mapper<PlayerHome>(dbFile("data.db"))
 * ```
 */
interface DataMapper<T> {

    // === 插入 ===
    fun insert(data: T)
    fun insertBatch(dataList: List<T>)
    fun insertAndGetKey(data: T): Long
    fun insertBatchAndGetKeys(dataList: List<T>): List<Long>

    // === 查询 ===
    fun findById(id: Any, filter: Filter.() -> Unit = {}): T?
    fun findAll(id: Any, filter: Filter.() -> Unit = {}): List<T>
    fun findOne(filter: Filter.() -> Unit = {}): T?
    fun findAll(filter: Filter.() -> Unit = {}): List<T>
    /** 批量查询，通过多个 @Id 值查询（IN 子句） */
    fun findByIds(ids: List<Any>): List<T>

    // === 基于 @Key 的查询 ===
    /** 通过 @Id + @Key 查询，返回所有匹配记录 */
    fun findByKey(data: T): List<T>
    /** 通过 @Id + @Key 查询，返回第一条匹配记录 */
    fun findOneByKey(data: T): T?
    /** 通过 @Id + @Key 检查记录是否存在 */
    fun existsByKey(data: T): Boolean
    /** 通过 @Id + @Key 删除匹配记录 */
    fun deleteByKey(data: T)

    // === 基于自增行 ID 的操作（用于无 @Id 字段的数据类）===
    /** 通过框架自动生成的 `id` 列查询 */
    fun findByRowId(rowId: Long): T?
    /** 通过框架自动生成的 `id` 列删除 */
    fun deleteByRowId(rowId: Long)

    // === 排序 ===
    fun sort(row: String, limit: Int = 10, filter: Filter.() -> Unit = {}): List<T>
    fun sortDescending(row: String, limit: Int = 10, filter: Filter.() -> Unit = {}): List<T>

    // === 更新 ===
    fun update(data: T, filter: Filter.() -> Unit = {})
    fun updateByKey(data: T)
    fun insertOrUpdate(data: T, filter: Filter.() -> Unit = {})
    fun upsertBatch(dataList: List<T>)
    /** 批量更新，通过 @Id + @Key 定位，使用 batch PreparedStatement */
    fun updateBatch(dataList: List<T>)

    // === 删除 ===
    fun deleteById(id: Any, filter: Filter.() -> Unit = {})
    fun deleteWhere(filter: Filter.() -> Unit)
    /** 批量删除，通过多个 @Id 值删除（IN 子句） */
    fun deleteByIds(ids: List<Any>)

    // === 检查 ===
    fun exists(id: Any, filter: Filter.() -> Unit = {}): Boolean
    fun exists(filter: Filter.() -> Unit): Boolean

    // === 计数 ===
    fun count(filter: Filter.() -> Unit = {}): Long

    // === 自定义 SQL ===

    /** 表名 */
    val tableName: String

    /**
     * 自定义 SELECT 查询，结果自动映射为 T
     *
     * ```kotlin
     * homeTable.query { where { "world" eq "world_nether" }; limit(10) }
     * ```
     */
    fun query(builder: ActionSelect.() -> Unit): List<T>

    /**
     * 自定义 SELECT 查询单条，结果自动映射为 T
     */
    fun queryOne(builder: ActionSelect.() -> Unit): T?

    /**
     * 自定义 SELECT 查询，自定义结果处理
     *
     * ```kotlin
     * homeTable.rawQuery({ rows("world"); groupBy("world") }) { rs ->
     *     buildList { while (rs.next()) add(rs.getString(1)) }
     * }
     * ```
     */
    fun <R> rawQuery(builder: ActionSelect.() -> Unit, handler: (ResultSet) -> R): R

    /**
     * 自定义 UPDATE 操作
     *
     * ```kotlin
     * homeTable.rawUpdate { set("active", false); where { "world" eq "world_nether" } }
     * ```
     * @return 受影响的行数
     */
    fun rawUpdate(builder: ActionUpdate.() -> Unit): Int

    /**
     * 自定义 DELETE 操作
     *
     * ```kotlin
     * homeTable.rawDelete { where { "active" eq false } }
     * ```
     * @return 受影响的行数
     */
    fun rawDelete(builder: ActionDelete.() -> Unit): Int

    /**
     * 执行任意 Action
     *
     * @return 受影响的行数
     */
    fun rawExecute(action: Action): Int

    // === 多表联查 ===

    /**
     * 发起多表联查，主表自动填充为当前 DataMapper 对应的表
     *
     * ```kotlin
     * homeTable.join {
     *     innerJoin<PlayerStats> {
     *         on("player_home.username" eq pre("player_stats.username"))
     *     }
     *     where { "player_home.active" eq true }
     *     limit(10)
     * }.mapTo<PlayerSummary>()
     * ```
     */
    fun join(builder: JoinQuery.() -> Unit): JoinQuery

    // === 事务 ===
    fun <R> transaction(block: DataMapper<T>.() -> R): Result<R>

    // === 生命周期 ===
    fun close()
}
