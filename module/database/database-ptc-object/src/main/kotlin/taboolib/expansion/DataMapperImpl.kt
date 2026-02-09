package taboolib.expansion

import taboolib.expansion.AnalyzedClassMember.Companion.toColumnName
import taboolib.module.database.*
import java.sql.ResultSet

/**
 * DataMapper 的标准实现
 *
 * 封装 ContainerOperator，提供类型安全的 CRUD 操作。
 * 支持可选的数据缓存，写入操作会按策略精确失效受影响的缓存。
 *
 * ### 缓存失效策略
 *
 * 缓存条目分为两类：
 * - **ID 定向缓存**：`findById`、`findAllById`、`exists` — 以特定 ID 为键
 * - **集合缓存**：`findAll`、`findOne`、`count`、`sort` 等 — 结果跨多条记录
 *
 * | 操作类型 | ID 定向缓存 | 集合缓存 |
 * |---------|-----------|---------|
 * | 单条更新/删除 | 仅失效该 ID | 全部失效 |
 * | 插入 | 保留 | 全部失效 |
 * | 批量/不确定范围 | 全部失效 | 全部失效 |
 *
 * @param type 数据类的 Class 对象
 * @param container 持久化容器
 * @param cache 数据缓存（可选）
 */
class DataMapperImpl<T>(
    private val type: Class<T>,
    private val container: PersistentContainer,
    private val cache: DataCache?
) : DataMapper<T> {

    private val operator: ContainerOperator
        get() = container[type.simpleName.toColumnName()]

    private val analyzedClass by lazy { AnalyzedClass.of(type) }

    // === 插入 ===

    override fun insert(data: T) {
        operator.insert(listOf(data as Any))
        invalidateCollections()
    }

    override fun insertBatch(dataList: List<T>) {
        if (dataList.isEmpty()) return
        @Suppress("UNCHECKED_CAST")
        operator.insert(dataList as List<Any>)
        invalidateCollections()
    }

    override fun insertAndGetKey(data: T): Long {
        val keys = operator.insertAndGetKeys(listOf(data as Any))
        invalidateCollections()
        return keys.firstOrNull() ?: -1L
    }

    override fun insertBatchAndGetKeys(dataList: List<T>): List<Long> {
        if (dataList.isEmpty()) return emptyList()
        @Suppress("UNCHECKED_CAST")
        val keys = operator.insertAndGetKeys(dataList as List<Any>)
        invalidateCollections()
        return keys
    }

    // === 查询 ===

    override fun findById(id: Any, filter: Filter.() -> Unit): T? {
        return cached("findById", id, filter) { operator.findOne(type, id, filter) }
    }

    override fun findAll(id: Any, filter: Filter.() -> Unit): List<T> {
        return cached("findAllById", id, filter) { operator.find(type, id, filter) }
    }

    override fun findOne(filter: Filter.() -> Unit): T? {
        return cached("findOne", filter) { operator.getOne(type, filter) }
    }

    override fun findAll(filter: Filter.() -> Unit): List<T> {
        return cached("findAll", filter) { operator.get(type, filter) }
    }

    override fun findByIds(ids: List<Any>): List<T> {
        if (ids.isEmpty()) return emptyList()
        return cached("findByIds", ids) { operator.findByIds(type, ids) }
    }

    // === 基于 @Key 的查询 ===

    override fun findByKey(data: T): List<T> {
        return cached("findByKey", data) { operator.findByKey(type, data as Any) }
    }

    override fun findOneByKey(data: T): T? {
        return cached("findOneByKey", data) { operator.findOneByKey(type, data as Any) }
    }

    override fun existsByKey(data: T): Boolean {
        return cached("existsByKey", data) { operator.hasByKey(type, data as Any) }
    }

    override fun deleteByKey(data: T) {
        operator.deleteByKey(data as Any)
        invalidateForData(data as Any)
    }

    // === 基于自增行 ID 的操作 ===

    override fun findByRowId(rowId: Long): T? {
        return cached("findByRowId", rowId) { operator.findByRowId(type, rowId) }
    }

    override fun deleteByRowId(rowId: Long) {
        operator.deleteByRowId(rowId)
        invalidateForRowId(rowId)
    }

    // === 排序 ===

    override fun sort(row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        return cached("sort", row, limit, filter) { operator.sort(type, row, limit, filter) }
    }

    override fun sortDescending(row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        return cached("sortDescending", row, limit, filter) { operator.sortDescending(type, row, limit, filter) }
    }

    // === 更新 ===

    override fun update(data: T, filter: Filter.() -> Unit) {
        operator.update(data as Any, true, filter)
        invalidateForData(data as Any)
    }

    override fun updateByKey(data: T) {
        operator.updateByKey(data as Any)
        invalidateForData(data as Any)
    }

    override fun insertOrUpdate(data: T, filter: Filter.() -> Unit) {
        operator.update(data as Any, true, filter)
        invalidateForData(data as Any)
    }

    override fun upsertBatch(dataList: List<T>) {
        if (dataList.isEmpty()) return
        @Suppress("UNCHECKED_CAST")
        operator.upsert(dataList as List<Any>)
        cache?.invalidateAll()
    }

    override fun updateBatch(dataList: List<T>) {
        if (dataList.isEmpty()) return
        @Suppress("UNCHECKED_CAST")
        operator.updateBatch(dataList as List<Any>)
        cache?.invalidateAll()
    }

    // === 删除 ===

    override fun deleteById(id: Any, filter: Filter.() -> Unit) {
        operator.delete(type, id, filter)
        invalidateForId(id)
    }

    override fun deleteWhere(filter: Filter.() -> Unit) {
        operator.deleteWhere(filter)
        cache?.invalidateAll()
    }

    override fun deleteByIds(ids: List<Any>) {
        if (ids.isEmpty()) return
        operator.deleteByIds(type, ids)
        cache?.invalidateAll()
    }

    // === 检查 ===

    override fun exists(id: Any, filter: Filter.() -> Unit): Boolean {
        return cached("exists", id, filter) { operator.has(type, id, filter) }
    }

    override fun exists(filter: Filter.() -> Unit): Boolean {
        return cached("existsFilter", filter) { operator.has(filter) }
    }

    // === 计数 ===

    override fun count(filter: Filter.() -> Unit): Long {
        return cached("count", filter) { operator.count(filter) }
    }

    // === 事务 ===

    override fun <R> transaction(block: DataMapper<T>.() -> R): Result<R> {
        return container.transaction {
            val txOperator = operator(type.simpleName.toColumnName())
            val txMapper = TransactionalDataMapper(type, txOperator, cache, connection)
            txMapper.block()
        }
    }

    // === 自定义 SQL ===

    override val tableName: String
        get() = operator.table.name

    override fun query(builder: ActionSelect.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        val action = ActionSelect(tableName).apply(builder)
        return operator.select(action) { rs ->
            buildList {
                while (rs.next()) { add(typeClass.createInstance<T>(typeClass.read(rs))) }
            }
        }
    }

    override fun queryOne(builder: ActionSelect.() -> Unit): T? {
        val typeClass = AnalyzedClass.of(type)
        val action = ActionSelect(tableName).apply { builder(); limit(1) }
        return operator.select(action) { rs ->
            if (rs.next()) typeClass.createInstance<T>(typeClass.read(rs)) else null
        }
    }

    override fun <R> rawQuery(builder: ActionSelect.() -> Unit, handler: (ResultSet) -> R): R {
        val action = ActionSelect(tableName).apply(builder)
        return operator.select(action, handler)
    }

    override fun rawUpdate(builder: ActionUpdate.() -> Unit): Int {
        val action = ActionUpdate(tableName).apply(builder)
        val result = operator.execute(action)
        cache?.invalidateAll()
        return result
    }

    override fun rawDelete(builder: ActionDelete.() -> Unit): Int {
        val action = ActionDelete(tableName).apply(builder)
        val result = operator.execute(action)
        cache?.invalidateAll()
        return result
    }

    override fun rawExecute(action: Action): Int {
        val result = operator.execute(action)
        cache?.invalidateAll()
        return result
    }

    // === 多表联查 ===

    override fun join(builder: JoinQuery.() -> Unit): JoinQuery {
        return container.join {
            from(tableName)
            builder()
        }
    }

    // === 生命周期 ===

    override fun close() = container.close()

    // === 缓存辅助 ===

    @Suppress("UNCHECKED_CAST")
    private fun <R> cached(method: String, vararg args: Any?, query: () -> R): R {
        if (cache == null) return query()
        val key = buildCacheKey(method, *args)
        return cache.get(key) { query() } as R
    }

    private fun buildCacheKey(method: String, vararg args: Any?): String {
        return "$method:${args.joinToString(",") { it?.toString() ?: "null" }}"
    }

    // === 缓存失效策略 ===

    companion object {
        /** 集合缓存前缀 — 任何数据变更都可能影响这些查询结果 */
        internal val COLLECTION_PREFIXES = arrayOf(
            "findOne:", "findAll:", "findByIds:",
            "findByKey:", "findOneByKey:", "existsByKey:", "existsFilter:",
            "sort:", "sortDescending:", "count:"
        )
    }

    /** 失效所有集合缓存，保留其他 ID 的定向缓存 */
    private fun invalidateCollections() {
        if (cache == null) return
        for (prefix in COLLECTION_PREFIXES) {
            cache.invalidateByPrefix(prefix)
        }
    }

    /** 失效指定 ID 的定向缓存 + 所有集合缓存 */
    private fun invalidateForId(id: Any) {
        if (cache == null) return
        cache.invalidateByPrefix("findById:$id")
        cache.invalidateByPrefix("findAllById:$id")
        cache.invalidateByPrefix("exists:$id")
        invalidateCollections()
    }

    /** 从数据对象提取 @Id 值，失效该 ID 的定向缓存 + 所有集合缓存 */
    private fun invalidateForData(data: Any) {
        if (cache == null) return
        val id = analyzedClass.getPrimaryMemberValue(data)
        if (id != null) {
            invalidateForId(id)
        } else {
            cache.invalidateAll()
        }
    }

    /** 失效指定行 ID 的缓存 + 所有集合缓存 */
    private fun invalidateForRowId(rowId: Long) {
        if (cache == null) return
        cache.invalidate(buildCacheKey("findByRowId", rowId))
        invalidateCollections()
    }
}