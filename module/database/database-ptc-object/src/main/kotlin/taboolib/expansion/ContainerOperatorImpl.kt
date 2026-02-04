package taboolib.expansion

import taboolib.common.platform.function.warning
import taboolib.module.database.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

/**
 * ContainerOperator 的标准实现
 *
 * 支持两种模式：
 * 1. 普通模式：每次操作从 DataSource 获取新连接（默认）
 * 2. 事务模式：所有操作共享同一个 Connection
 *
 * @author 坏黑
 * @since 2023/3/29 13:29
 */
class ContainerOperatorImpl(
    override val table: Table<*, *>,
    override val dataSource: DataSource,
    private val sharedConnection: Connection? = null
) : ContainerOperator() {

    /**
     * 创建事务模式的操作器（共享 Connection）
     */
    fun withConnection(connection: Connection): ContainerOperatorImpl {
        return ContainerOperatorImpl(table, dataSource, connection)
    }

    /**
     * 是否为事务模式
     */
    val isTransactional: Boolean
        get() = sharedConnection != null

    override fun <T> getOne(type: Class<T>, filter: Filter.() -> Unit): T? {
        val typeClass = AnalyzedClass.of(type)
        val action = ActionSelect(table.name).apply {
            limit(1)
            where(filter)
        }
        return executeQuery(action) { rs ->
            if (rs.next()) typeClass.createInstance<T>(typeClass.read(rs)) else null
        }
    }

    override fun <T> get(type: Class<T>, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        val action = ActionSelect(table.name).apply {
            where(filter)
        }
        return executeQuery(action) { rs ->
            buildList {
                while (rs.next()) {
                    add(typeClass.createInstance<T>(typeClass.read(rs)))
                }
            }
        }
    }

    override fun <T> findOne(type: Class<T>, id: Any, filter: Filter.() -> Unit): T? {
        val typeClass = AnalyzedClass.of(type)
        return executeQuery(selectById(typeClass, id, filter) { limit(1) }) { rs ->
            if (rs.next()) typeClass.createInstance<T>(typeClass.read(rs)) else null
        }
    }

    override fun <T> find(type: Class<T>, id: Any, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        return executeQuery(selectById(typeClass, id, filter)) { rs ->
            buildList {
                while (rs.next()) {
                    add(typeClass.createInstance<T>(typeClass.read(rs)))
                }
            }
        }
    }

    override fun <T> sort(type: Class<T>, row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        val action = ActionSelect(table.name).apply {
            where(filter)
            limit(limit)
            orderBy(row)
        }
        return executeQuery(action) { rs ->
            buildList {
                while (rs.next()) {
                    add(typeClass.createInstance<T>(typeClass.read(rs)))
                }
            }
        }
    }

    override fun <T> sortDescending(type: Class<T>, row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        val action = ActionSelect(table.name).apply {
            where(filter)
            limit(limit)
            orderBy(row, Order.Type.DESC)
        }
        return executeQuery(action) { rs ->
            buildList {
                while (rs.next()) {
                    add(typeClass.createInstance<T>(typeClass.read(rs)))
                }
            }
        }
    }

    override fun <T> has(type: Class<T>, id: Any, filter: Filter.() -> Unit): Boolean {
        return executeQuery(selectById(AnalyzedClass.of(type), id, filter) { limit(1) }) { it.next() }
    }

    override fun has(filter: Filter.() -> Unit): Boolean {
        val action = ActionSelect(table.name).apply {
            limit(1)
            where(filter)
        }
        return executeQuery(action) { it.next() }
    }

    override fun insert(dataList: List<Any>) {
        if (dataList.isEmpty()) return
        val typeClass = AnalyzedClass.of(dataList.first().javaClass)
        val action = ActionInsert(table.name, typeClass.members.map { it.name }.toTypedArray()).apply {
            dataList.forEach { data ->
                values(typeClass.members.map { member -> typeClass.getValue(data, member)?.value() })
            }
        }
        executeUpdate(action)
    }

    override fun update(data: Any, usePrimaryKey: Boolean, filter: Filter.() -> Unit) {
        val typeClass = AnalyzedClass.of(data::class.java)
        if (typeClass.members.none { !it.isFinal }) {
            error("No mutable field found.")
        }
        val name = if (usePrimaryKey) typeClass.primaryMemberName ?: error("No primary id found.") else null
        val value = if (usePrimaryKey) typeClass.getPrimaryMemberValue(data) else null
        // check-then-act 包裹在事务中，防止并发双重 INSERT
        withTransaction { conn ->
            val existsAction = ActionSelect(table.name).apply {
                limit(1)
                if (name != null) where(name eq value?.value())
                where(filter)
            }
            val exists = executeQueryWith(conn, existsAction) { it.next() }
            if (exists) {
                val updateAction = ActionUpdate(table.name).apply {
                    if (name != null) where(name eq value?.value())
                    where(filter)
                    typeClass.members.filter { !it.isFinal }.forEach { member ->
                        set(member.name, typeClass.getValue(data, member)?.value())
                    }
                }
                conn.executePrepared(updateAction.query, updateAction.elements, Statement.RETURN_GENERATED_KEYS) { executeUpdate() }
            } else {
                val insertTypeClass = AnalyzedClass.of(data.javaClass)
                val insertAction = ActionInsert(table.name, insertTypeClass.members.map { it.name }.toTypedArray()).apply {
                    values(insertTypeClass.members.map { member -> insertTypeClass.getValue(data, member)?.value() })
                }
                conn.executePrepared(insertAction.query, insertAction.elements, Statement.RETURN_GENERATED_KEYS) { executeUpdate() }
            }
        }
    }

    override fun updateByKey(data: Any, usePrimaryKey: Boolean) {
        val typeClass = AnalyzedClass.of(data::class.java)
        if (typeClass.members.none { !it.isFinal }) {
            error("No mutable field found.")
        }
        update(data, usePrimaryKey) {
            typeClass.members.filter { it.isKey }.forEach { member ->
                member.name eq typeClass.getValue(data, member)?.value()
            }
        }
    }

    override fun upsert(dataList: List<Any>) {
        if (dataList.isEmpty()) return
        val typeClass = AnalyzedClass.of(dataList.first().javaClass)
        if (typeClass.members.none { !it.isFinal }) {
            error("No mutable field found.")
        }
        val primaryName = typeClass.primaryMemberName ?: error("No primary id found.")
        val keyMembers = typeClass.members.filter { it.isKey }
        val mutableMembers = typeClass.members.filter { !it.isFinal }
        // 构建唯一标识：@Id + @Key 的值
        fun buildKey(data: Any): String {
            val id = typeClass.getPrimaryMemberValue(data)?.value().toString()
            val keys = keyMembers.joinToString("|") { typeClass.getValue(data, it)?.value().toString() }
            return "$id|$keys"
        }
        withTransaction { conn ->
            // 1. 批量查询存在的记录
            val existingKeys = hashSetOf<String>()
            if (keyMembers.isNotEmpty()) {
                // 有 @Key：需要精确匹配 @Id + @Key
                dataList.forEach { data ->
                    val checkAction = ActionSelect(table.name).apply {
                        limit(1)
                        where(primaryName eq typeClass.getPrimaryMemberValue(data)?.value())
                        keyMembers.forEach { member ->
                            where(member.name eq typeClass.getValue(data, member)?.value())
                        }
                    }
                    val exists = executeQueryWith(conn, checkAction) { it.next() }
                    if (exists) {
                        existingKeys += buildKey(data)
                    }
                }
            } else {
                // 无 @Key：只匹配 @Id，使用 IN 查询优化
                val ids = dataList.mapNotNull { typeClass.getPrimaryMemberValue(it)?.value() }.toTypedArray()
                val action = ActionSelect(table.name).apply {
                    where { primaryName inside ids }
                }
                executeQueryWith(conn, action) { rs ->
                    while (rs.next()) {
                        existingKeys += rs.getObject(primaryName)?.toString() ?: ""
                    }
                }
            }
            // 2. 分流：存在的更新，不存在的插入
            val toInsert = mutableListOf<Any>()
            val toUpdate = mutableListOf<Any>()
            dataList.forEach { data ->
                val key = if (keyMembers.isNotEmpty()) {
                    buildKey(data)
                } else {
                    typeClass.getPrimaryMemberValue(data)?.value().toString()
                }
                if (key in existingKeys) {
                    toUpdate += data
                } else {
                    toInsert += data
                }
            }
            // 3. 批量插入
            if (toInsert.isNotEmpty()) {
                val insertSql = buildInsertSql(typeClass)
                conn.prepareStatement(insertSql).use { stmt ->
                    toInsert.forEach { data ->
                        typeClass.members.forEachIndexed { i, member ->
                            stmt.setObject(i + 1, typeClass.getValue(data, member)?.value())
                        }
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }
            }
            // 4. 批量更新
            if (toUpdate.isNotEmpty()) {
                val updateSql = buildUpdateSql(typeClass, primaryName, keyMembers)
                conn.prepareStatement(updateSql).use { stmt ->
                    toUpdate.forEach { data ->
                        var idx = 1
                        // SET 子句的值
                        mutableMembers.forEach { member ->
                            stmt.setObject(idx++, typeClass.getValue(data, member)?.value())
                        }
                        // WHERE 子句的值
                        stmt.setObject(idx++, typeClass.getPrimaryMemberValue(data)?.value())
                        keyMembers.forEach { member ->
                            stmt.setObject(idx++, typeClass.getValue(data, member)?.value())
                        }
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }
            }
        }
    }

    /**
     * 构建 INSERT SQL
     */
    private fun buildInsertSql(typeClass: AnalyzedClass): String {
        val columns = typeClass.members.joinToString(", ") { "`${it.name}`" }
        val placeholders = typeClass.members.joinToString(", ") { "?" }
        return "INSERT INTO `${table.name}` ($columns) VALUES ($placeholders)"
    }

    /**
     * 构建 UPDATE SQL
     */
    private fun buildUpdateSql(
        typeClass: AnalyzedClass,
        primaryName: String,
        keyMembers: List<AnalyzedClassMember>
    ): String {
        val mutableMembers = typeClass.members.filter { !it.isFinal }
        val setClause = mutableMembers.joinToString(", ") { "`${it.name}` = ?" }
        val whereClause = buildString {
            append("`$primaryName` = ?")
            keyMembers.forEach { append(" AND `${it.name}` = ?") }
        }
        return "UPDATE `${table.name}` SET $setClause WHERE $whereClause"
    }

    override fun <T> delete(type: Class<T>, id: Any, filter: Filter.() -> Unit) {
        val typeClass = AnalyzedClass.of(type)
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        val action = ActionDelete(table.name).apply {
            where(name eq id.value())
            where(filter)
        }
        executeUpdate(action)
    }

    /**
     * 构建基于 @Id 的查询
     */
    private fun selectById(
        typeClass: AnalyzedClass,
        id: Any,
        filter: Filter.() -> Unit,
        extra: ActionSelect.() -> Unit = {}
    ): ActionSelect {
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        return ActionSelect(table.name).apply {
            where(name eq id.value())
            where(filter)
            extra()
        }
    }

    /**
     * 执行查询
     */
    private fun <T> executeQuery(action: ActionSelect, handler: (ResultSet) -> T): T {
        return withConnection { conn -> executeQueryWith(conn, action, handler) }
    }

    /**
     * 执行更新
     */
    private fun executeUpdate(action: Action): Int {
        return withConnection { conn ->
            conn.executePrepared(action.query, action.elements, Statement.RETURN_GENERATED_KEYS) { executeUpdate() }
        }
    }

    /**
     * 获取连接并执行操作
     */
    private inline fun <T> withConnection(crossinline block: (Connection) -> T): T {
        return if (sharedConnection != null) {
            block(sharedConnection)
        } else {
            dataSource.connection.use { block(it) }
        }
    }

    /**
     * 在事务中执行操作（用于批量写入）
     */
    private inline fun withTransaction(crossinline block: (Connection) -> Unit) {
        if (sharedConnection != null) {
            // 已在事务中，直接执行
            block(sharedConnection)
        } else {
            dataSource.connection.use { conn ->
                val originalAutoCommit = conn.autoCommit
                conn.autoCommit = false
                try {
                    block(conn)
                    conn.commit()
                } catch (e: Exception) {
                    conn.rollback()
                    throw e
                } finally {
                    conn.autoCommit = originalAutoCommit
                }
            }
        }
    }

    /**
     * 使用指定连接执行查询
     */
    private fun <T> executeQueryWith(conn: Connection, action: ActionSelect, handler: (ResultSet) -> T): T {
        return conn.executePrepared(action.query, action.elements) { executeQuery().use(handler) }
    }

    /**
     * 执行 PreparedStatement 的通用包装，处理参数绑定和错误日志
     */
    private inline fun <T> Connection.executePrepared(
        query: String,
        elements: List<Any?>,
        flags: Int = 0,
        crossinline block: PreparedStatement.() -> T
    ): T {
        return try {
            (if (flags != 0) prepareStatement(query, flags) else prepareStatement(query)).use { stmt ->
                elements.forEachIndexed { i, v -> stmt.setObject(i + 1, v) }
                stmt.block()
            }
        } catch (ex: SQLException) {
            warning("Query: $query")
            warning("Parameters (${elements.size}): $elements")
            throw ex
        }
    }
}
