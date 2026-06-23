package taboolib.expansion

import taboolib.module.database.*
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
    /** ⚠ SQLite 限制：批量插入时仅返回最后一条记录的自增 ID，返回列表长度可能为 1 而非 dataList.size */
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

    // === 分页 ===
    /** 分页获取数据，返回包含总数信息的 [Page] 对象 */
    fun findPage(page: Int, size: Int, filter: Filter.() -> Unit = {}): Page<T>
    /** 分页正序排序，返回包含总数信息的 [Page] 对象 */
    fun sortPage(row: String, page: Int, size: Int, filter: Filter.() -> Unit = {}): Page<T>
    /** 分页倒序排序，返回包含总数信息的 [Page] 对象 */
    fun sortDescendingPage(row: String, page: Int, size: Int, filter: Filter.() -> Unit = {}): Page<T>

    // === 游标查询（必须在 transaction {} 中使用）===

    /**
     * 游标查询，逐行读取数据，避免大量数据导致内存溢出。
     *
     * **必须在 [transaction] 中使用**，否则抛出异常。
     *
     * ```kotlin
     * homeMapper.transaction {
     *     selectCursor { "world" eq "overworld" }.forEach { home ->
     *         // 逐条处理
     *     }
     * }
     * ```
     */
    fun selectCursor(filter: Filter.() -> Unit = {}): Cursor<T>

    /** 游标查询（正序排序），必须在 [transaction] 中使用 */
    fun sortCursor(row: String, filter: Filter.() -> Unit = {}): Cursor<T>

    /** 游标查询（倒序排序），必须在 [transaction] 中使用 */
    fun sortDescendingCursor(row: String, filter: Filter.() -> Unit = {}): Cursor<T>

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

    // === 容器类型 Accessor ===

    /**
     * 获取 Map 类型容器字段的数据库代理（通过 @Id 定位父记录）
     *
     * ```kotlin
     * val props: MutableMap<String, String?> = mapper.mapOf("player1", "props")
     * props["key1"] = "value1"   // → SQL INSERT/UPDATE
     * props["key1"]              // → SQL SELECT
     * ```
     */
    fun mapOf(id: Any, fieldName: String): MutableMap<String, String?>

    /**
     * 获取 Map 类型容器字段的数据库代理（通过 Filter 定位父记录）
     */
    fun mapOf(fieldName: String, filter: Filter.() -> Unit): MutableMap<String, String?>

    /**
     * 获取 List 类型容器字段的数据库代理（通过 @Id 定位父记录）
     *
     * ```kotlin
     * val tags: MutableList<String?> = mapper.listOf("player1", "tags")
     * tags.add("newTag")         // → SQL INSERT
     * tags[0]                    // → SQL SELECT
     * ```
     */
    fun listOf(id: Any, fieldName: String): MutableList<String?>

    /**
     * 获取 List 类型容器字段的数据库代理（通过 Filter 定位父记录）
     */
    fun listOf(fieldName: String, filter: Filter.() -> Unit): MutableList<String?>

    /**
     * 获取 Set 类型容器字段的数据库代理（通过 @Id 定位父记录）
     *
     * ```kotlin
     * val scores: MutableSet<String?> = mapper.setOf("player1", "scores")
     * scores.add("100")          // → SQL INSERT IF NOT EXISTS
     * scores.contains("100")     // → SQL SELECT EXISTS
     * ```
     */
    fun setOf(id: Any, fieldName: String): MutableSet<String?>

    /**
     * 获取 Set 类型容器字段的数据库代理（通过 Filter 定位父记录）
     */
    fun setOf(fieldName: String, filter: Filter.() -> Unit): MutableSet<String?>
}
