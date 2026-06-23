package taboolib.expansion.mapper

import taboolib.expansion.*
import taboolib.expansion.operator.ContainerOperatorImpl
import taboolib.expansion.orm.AnalyzedClass
import taboolib.expansion.orm.EntityMapper
import taboolib.module.database.*
import java.sql.ResultSet

/**
 * DataMapper 公共基类（Template Method 模式）
 *
 * 将 DataMapperImpl 和 TransactionalDataMapper 的公共 CRUD 实现合并到此基类，
 * 子类通过覆写差异方法来实现各自的特定行为：
 * - operator 来源（动态查找 vs 构造注入）
 * - 读操作是否走缓存
 * - 游标查询支持
 * - 事务、join、close 行为
 */
abstract class AbstractDataMapper<T> : DataMapper<T> {

    protected abstract val type: Class<T>
    protected abstract val operator: ContainerOperator
    protected abstract val cache: L2Cache?

    protected val analyzedClass by lazy { AnalyzedClass.of(type) }

    // === 读操作分派（默认直接执行，子类可覆写为走缓存）===

    protected open fun <R> executeRead(key: String, vararg args: Any?, query: () -> R): R = query()
    protected open fun <R> executeBeanRead(key: String, vararg args: Any?, query: () -> R): R = query()

    // === 插入 ===

    override fun insert(data: T) {
        operator.insert(listOf(data as Any))
        invalidateOnInsert()
    }

    override fun insertBatch(dataList: List<T>) {
        if (dataList.isEmpty()) return
        @Suppress("UNCHECKED_CAST")
        operator.insert(dataList as List<Any>)
        invalidateOnInsert()
    }

    override fun insertAndGetKey(data: T): Long {
        val keys = operator.insertAndGetKeys(listOf(data as Any))
        invalidateOnInsert()
        return keys.firstOrNull() ?: -1L
    }

    override fun insertBatchAndGetKeys(dataList: List<T>): List<Long> {
        if (dataList.isEmpty()) return emptyList()
        @Suppress("UNCHECKED_CAST")
        val keys = operator.insertAndGetKeys(dataList as List<Any>)
        invalidateOnInsert()
        return keys
    }

    // === 查询 ===

    override fun findById(id: Any, filter: Filter.() -> Unit): T? {
        return executeBeanRead("findById", id, filter) { operator.findOne(type, id, filter) }
    }

    override fun findAll(id: Any, filter: Filter.() -> Unit): List<T> {
        return executeBeanRead("findAllById", id, filter) { operator.find(type, id, filter) }
    }

    override fun findOne(filter: Filter.() -> Unit): T? {
        return executeRead("findOne", filter) { operator.getOne(type, filter) }
    }

    override fun findAll(filter: Filter.() -> Unit): List<T> {
        return executeRead("findAll", filter) { operator.get(type, filter) }
    }

    override fun findByIds(ids: List<Any>): List<T> {
        if (ids.isEmpty()) return emptyList()
        return executeRead("findByIds", ids) { operator.findByIds(type, ids) }
    }

    // === 基于 @Key 的查询 ===

    override fun findByKey(data: T): List<T> {
        return executeRead("findByKey", data) { operator.findByKey(type, data as Any) }
    }

    override fun findOneByKey(data: T): T? {
        return executeRead("findOneByKey", data) { operator.findOneByKey(type, data as Any) }
    }

    override fun existsByKey(data: T): Boolean {
        return executeRead("existsByKey", data) { operator.hasByKey(type, data as Any) }
    }

    override fun deleteByKey(data: T) {
        operator.deleteByKey(data as Any)
        invalidateOnMutation(data as Any)
    }

    // === 基于自增行 ID 的操作 ===

    override fun findByRowId(rowId: Long): T? {
        return executeBeanRead("findByRowId", rowId) { operator.findByRowId(type, rowId) }
    }

    override fun deleteByRowId(rowId: Long) {
        operator.deleteByRowId(rowId)
        invalidateOnRowIdMutation(rowId)
    }

    // === 排序 ===

    override fun sort(row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        return executeRead("sort", row, limit, filter) { operator.sort(type, row, limit, filter) }
    }

    override fun sortDescending(row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        return executeRead("sortDescending", row, limit, filter) { operator.sortDescending(type, row, limit, filter) }
    }

    // === 分页 ===

    override fun findPage(page: Int, size: Int, filter: Filter.() -> Unit): Page<T> {
        val content = executeRead("findPage", page, size, filter) { operator.getPage(type, page, size, filter) }
        val total = executeRead("count", filter) { operator.count(filter) }
        return Page(content, page, size, total)
    }

    override fun sortPage(row: String, page: Int, size: Int, filter: Filter.() -> Unit): Page<T> {
        val content = executeRead("sortPage", row, page, size, filter) { operator.sortPage(type, row, page, size, filter) }
        val total = executeRead("count", filter) { operator.count(filter) }
        return Page(content, page, size, total)
    }

    override fun sortDescendingPage(row: String, page: Int, size: Int, filter: Filter.() -> Unit): Page<T> {
        val content = executeRead("sortDescendingPage", row, page, size, filter) { operator.sortDescendingPage(type, row, page, size, filter) }
        val total = executeRead("count", filter) { operator.count(filter) }
        return Page(content, page, size, total)
    }

    // === 游标查询（默认不支持，子类可覆写）===

    override fun selectCursor(filter: Filter.() -> Unit): Cursor<T> {
        error("游标查询必须在 transaction {} 中使用")
    }

    override fun sortCursor(row: String, filter: Filter.() -> Unit): Cursor<T> {
        error("游标查询必须在 transaction {} 中使用")
    }

    override fun sortDescendingCursor(row: String, filter: Filter.() -> Unit): Cursor<T> {
        error("游标查询必须在 transaction {} 中使用")
    }

    // === 更新 ===

    override fun update(data: T, filter: Filter.() -> Unit) {
        operator.update(data as Any, true, filter)
        invalidateOnMutation(data as Any)
    }

    override fun updateByKey(data: T) {
        operator.updateByKey(data as Any)
        invalidateOnMutation(data as Any)
    }

    override fun insertOrUpdate(data: T, filter: Filter.() -> Unit) {
        operator.update(data as Any, true, filter)
        invalidateOnMutation(data as Any)
    }

    override fun upsertBatch(dataList: List<T>) {
        if (dataList.isEmpty()) return
        @Suppress("UNCHECKED_CAST")
        operator.upsert(dataList as List<Any>)
        invalidateAll()
    }

    override fun updateBatch(dataList: List<T>) {
        if (dataList.isEmpty()) return
        @Suppress("UNCHECKED_CAST")
        operator.updateBatch(dataList as List<Any>)
        invalidateAll()
    }

    // === 删除 ===

    override fun deleteById(id: Any, filter: Filter.() -> Unit) {
        operator.delete(type, id, filter)
        invalidateOnIdMutation(id)
    }

    override fun deleteWhere(filter: Filter.() -> Unit) {
        operator.deleteWhere(filter)
        invalidateAll()
    }

    override fun deleteByIds(ids: List<Any>) {
        if (ids.isEmpty()) return
        operator.deleteByIds(type, ids)
        invalidateAll()
    }

    // === 检查 ===

    override fun exists(id: Any, filter: Filter.() -> Unit): Boolean {
        return executeBeanRead("exists", id, filter) { operator.has(type, id, filter) }
    }

    override fun exists(filter: Filter.() -> Unit): Boolean {
        return executeRead("existsFilter", filter) { operator.has(filter) }
    }

    // === 计数 ===

    override fun count(filter: Filter.() -> Unit): Long {
        return executeRead("count", filter) { operator.count(filter) }
    }

    // === 自定义 SQL ===

    override val tableName: String
        get() = operator.table.name

    override fun query(builder: ActionSelect.() -> Unit): List<T> {
        val mapper = EntityMapper.of(type)
        val action = ActionSelect(tableName).apply(builder)
        return operator.select(action) { rs ->
            buildList {
                while (rs.next()) { add(mapper.createInstance(mapper.read(rs))) }
            }
        }
    }

    override fun queryOne(builder: ActionSelect.() -> Unit): T? {
        val mapper = EntityMapper.of(type)
        val action = ActionSelect(tableName).apply { builder(); limit(1) }
        return operator.select(action) { rs ->
            if (rs.next()) mapper.createInstance(mapper.read(rs)) else null
        }
    }

    override fun <R> rawQuery(builder: ActionSelect.() -> Unit, handler: (ResultSet) -> R): R {
        val action = ActionSelect(tableName).apply(builder)
        return operator.select(action, handler)
    }

    override fun rawUpdate(builder: ActionUpdate.() -> Unit): Int {
        val action = ActionUpdate(tableName).apply(builder)
        val result = operator.execute(action)
        invalidateAll()
        return result
    }

    override fun rawDelete(builder: ActionDelete.() -> Unit): Int {
        val action = ActionDelete(tableName).apply(builder)
        val result = operator.execute(action)
        invalidateAll()
        return result
    }

    override fun rawExecute(action: Action): Int {
        val result = operator.execute(action)
        invalidateAll()
        return result
    }

    // === 容器类型 Accessor ===

    override fun mapOf(id: Any, fieldName: String): MutableMap<String, String?> {
        return (operator as ContainerOperatorImpl).mapAccessor(id, fieldName)
    }

    override fun mapOf(fieldName: String, filter: Filter.() -> Unit): MutableMap<String, String?> {
        return (operator as ContainerOperatorImpl).mapAccessor(fieldName, filter)
    }

    override fun listOf(id: Any, fieldName: String): MutableList<String?> {
        return (operator as ContainerOperatorImpl).listAccessor(id, fieldName)
    }

    override fun listOf(fieldName: String, filter: Filter.() -> Unit): MutableList<String?> {
        return (operator as ContainerOperatorImpl).listAccessor(fieldName, filter)
    }

    override fun setOf(id: Any, fieldName: String): MutableSet<String?> {
        return (operator as ContainerOperatorImpl).setAccessor(id, fieldName)
    }

    override fun setOf(fieldName: String, filter: Filter.() -> Unit): MutableSet<String?> {
        return (operator as ContainerOperatorImpl).setAccessor(fieldName, filter)
    }

    // === 缓存辅助 ===

    @Suppress("UNCHECKED_CAST")
    protected fun <R> cachedBean(method: String, vararg args: Any?, query: () -> R): R {
        val c = cache ?: return query()
        val key = buildCacheKey(method, *args)
        return c.beanCache.get(key) { query() } as R
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <R> cachedQuery(method: String, vararg args: Any?, query: () -> R): R {
        val c = cache ?: return query()
        val key = buildCacheKey(method, *args)
        return c.queryCache.get(key) { query() } as R
    }

    protected fun buildCacheKey(method: String, vararg args: Any?): String {
        return "$method:${args.joinToString(",") { it?.toString() ?: "null" }}"
    }

    // === L2 缓存失效策略 ===

    /** 插入后：仅清空 Query Cache */
    protected fun invalidateOnInsert() {
        cache?.queryCache?.invalidateAll()
    }

    /** 单条更新/删除后（按 ID）：失效该 ID 的 Bean Cache + 清空 Query Cache */
    protected fun invalidateOnIdMutation(id: Any) {
        val c = cache ?: return
        c.beanCache.invalidateByPrefix("findById:$id")
        c.beanCache.invalidateByPrefix("findAllById:$id")
        c.beanCache.invalidateByPrefix("exists:$id")
        c.queryCache.invalidateAll()
    }

    /** 从数据对象提取 @Id 值，失效该 ID 的 Bean Cache + 清空 Query Cache */
    protected fun invalidateOnMutation(data: Any) {
        if (cache == null) return
        val id = analyzedClass.getPrimaryMemberValue(data)
        if (id != null) {
            invalidateOnIdMutation(id)
        } else {
            invalidateAll()
        }
    }

    /** 失效指定行 ID 的 Bean Cache + 清空 Query Cache */
    protected fun invalidateOnRowIdMutation(rowId: Long) {
        val c = cache ?: return
        c.beanCache.invalidate(buildCacheKey("findByRowId", rowId))
        c.queryCache.invalidateAll()
    }

    /** 批量/不确定范围：全部清空 */
    protected fun invalidateAll() {
        cache?.invalidateAll()
    }
}
