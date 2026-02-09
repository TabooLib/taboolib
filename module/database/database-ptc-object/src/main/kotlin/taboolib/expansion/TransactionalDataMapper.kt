package taboolib.expansion

import taboolib.module.database.*
import java.sql.Connection
import java.sql.ResultSet

/**
 * 事务内的 DataMapper 实现
 *
 * 使用事务感知的 ContainerOperator，所有操作共享同一个数据库连接。
 * 不支持嵌套事务，调用 transaction() 会抛出异常。
 *
 * 事务内不使用缓存读取（保证事务一致性），但写入操作会失效外层缓存。
 */
class TransactionalDataMapper<T>(
    private val type: Class<T>,
    private val operator: ContainerOperator,
    private val cache: DataCache?,
    private val sharedConnection: Connection? = null
) : DataMapper<T> {

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

    // === 查询（事务内不使用缓存，直接查库）===

    override fun findById(id: Any, filter: Filter.() -> Unit): T? = operator.findOne(type, id, filter)
    override fun findAll(id: Any, filter: Filter.() -> Unit): List<T> = operator.find(type, id, filter)
    override fun findOne(filter: Filter.() -> Unit): T? = operator.getOne(type, filter)
    override fun findAll(filter: Filter.() -> Unit): List<T> = operator.get(type, filter)

    override fun findByIds(ids: List<Any>): List<T> {
        if (ids.isEmpty()) return emptyList()
        return operator.findByIds(type, ids)
    }

    // === 基于 @Key 的查询 ===

    override fun findByKey(data: T): List<T> = operator.findByKey(type, data as Any)
    override fun findOneByKey(data: T): T? = operator.findOneByKey(type, data as Any)
    override fun existsByKey(data: T): Boolean = operator.hasByKey(type, data as Any)

    override fun deleteByKey(data: T) {
        operator.deleteByKey(data as Any)
        invalidateForData(data as Any)
    }

    // === 基于自增行 ID 的操作 ===

    override fun findByRowId(rowId: Long): T? = operator.findByRowId(type, rowId)

    override fun deleteByRowId(rowId: Long) {
        operator.deleteByRowId(rowId)
        invalidateForRowId(rowId)
    }

    // === 排序 ===

    override fun sort(row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        return operator.sort(type, row, limit, filter)
    }

    override fun sortDescending(row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        return operator.sortDescending(type, row, limit, filter)
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

    override fun exists(id: Any, filter: Filter.() -> Unit): Boolean = operator.has(type, id, filter)
    override fun exists(filter: Filter.() -> Unit): Boolean = operator.has(filter)

    // === 计数 ===

    override fun count(filter: Filter.() -> Unit): Long = operator.count(filter)

    // === 事务 ===

    override fun <R> transaction(block: DataMapper<T>.() -> R): Result<R> {
        error("Nested transactions are not supported")
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
        return JoinQuery(operator.dataSource, sharedConnection).also {
            it.from(tableName)
            builder(it)
        }
    }

    // === 生命周期 ===

    override fun close() {
        error("Cannot close container within a transaction")
    }

    // === 缓存失效策略 ===

    private fun buildCacheKey(method: String, vararg args: Any?): String {
        return "$method:${args.joinToString(",") { it?.toString() ?: "null" }}"
    }

    /** 失效所有集合缓存，保留其他 ID 的定向缓存 */
    private fun invalidateCollections() {
        if (cache == null) return
        for (prefix in DataMapperImpl.COLLECTION_PREFIXES) {
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