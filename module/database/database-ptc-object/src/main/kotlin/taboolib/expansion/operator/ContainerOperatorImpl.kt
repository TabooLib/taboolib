package taboolib.expansion.operator

import taboolib.common.platform.function.warning
import taboolib.expansion.*
import taboolib.expansion.orm.AnalyzedClass
import taboolib.expansion.orm.AnalyzedClassMember
import taboolib.expansion.orm.EntityMapper
import taboolib.module.database.*
import java.sql.*
import java.sql.Statement
import javax.sql.DataSource

/**
 * ContainerOperator 的标准实现（Facade）
 *
 * 支持两种模式：
 * 1. 普通模式：每次操作从 DataSource 获取新连接（默认）
 * 2. 事务模式：所有操作共享同一个 Connection
 *
 * 将 @LinkTable 级联操作委托给 [LinkTableHandler]，
 * 将 Collection 子表操作委托给 [CollectionTableHandler]。
 *
 * @author 坏黑
 * @since 2023/3/29 13:29
 */
class ContainerOperatorImpl(
    override val table: Table<*, *>,
    override val dataSource: DataSource,
    private val sharedConnection: Connection? = null,
    private val classTableMap: Map<Class<*>, String>? = null,
    private val classOperatorMap: Map<Class<*>, ContainerOperator>? = null,
    private val collectionTableInfos: List<CollectionTableInfo>? = null
) : ContainerOperator(), DataExecutor {

    /**
     * 创建事务模式的操作器（共享 Connection）
     */
    fun withConnection(connection: Connection): ContainerOperatorImpl {
        return ContainerOperatorImpl(table, dataSource, connection, classTableMap, classOperatorMap, collectionTableInfos)
    }

    /**
     * 是否为事务模式
     */
    val isTransactional: Boolean
        get() = sharedConnection != null

    // === Handler 懒加载 ===

    private val linkHandler: LinkTableHandler? by lazy {
        if (classTableMap != null)
            LinkTableHandler(classTableMap, classOperatorMap ?: emptyMap(), this)
        else null
    }

    private val collectionHandler: CollectionTableHandler? by lazy {
        if (!collectionTableInfos.isNullOrEmpty())
            CollectionTableHandler(collectionTableInfos, this)
        else null
    }

    // === DataExecutor 接口实现 ===

    override fun setupQuoter() {
        setupQuoterForHost(table.host)
    }

    override fun <T> withConnection(block: (Connection) -> T): T {
        setupQuoter()
        return when {
            sharedConnection != null -> block(sharedConnection)
            TransactionContext.currentConnection.get() != null -> block(TransactionContext.currentConnection.get())
            else -> dataSource.connection.use { block(it) }
        }
    }

    override fun withTransaction(block: (Connection) -> Unit) {
        setupQuoter()
        when {
            sharedConnection != null -> block(sharedConnection)
            TransactionContext.currentConnection.get() != null -> block(TransactionContext.currentConnection.get())
            else -> {
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
    }

    override fun <T> executeQuery(action: ActionSelect, handler: (ResultSet) -> T): T {
        return withConnection { conn -> executeQueryWith(conn, action, handler) }
    }

    override fun <T> executeQueryWith(conn: Connection, action: ActionSelect, handler: (ResultSet) -> T): T {
        return executePrepared(conn, action.query, action.elements) { executeQuery().use(handler) }
    }

    override fun executeUpdate(action: Action): Int {
        return withConnection { conn ->
            executePrepared(conn, action.query, action.elements, Statement.RETURN_GENERATED_KEYS) { executeUpdate() }
        }
    }

    override fun <T> executePrepared(conn: Connection, query: String, elements: List<Any?>, flags: Int, block: PreparedStatement.() -> T): T {
        return try {
            (if (flags != 0) conn.prepareStatement(query, flags) else conn.prepareStatement(query)).use { stmt ->
                elements.forEachIndexed { i, v -> stmt.setObject(i + 1, convertParam(v)) }
                stmt.block()
            }
        } catch (ex: SQLException) {
            warning("Query: $query")
            warning("Parameters (${elements.size}): $elements")
            throw ex
        }
    }

    override fun convertParam(value: Any?): Any? {
        return when (value) {
            is IndexedEnum -> value.index
            is Enum<*> -> value.name
            else -> value
        }
    }

    // === CRUD 操作 ===

    override fun <T> getOne(type: Class<T>, filter: Filter.() -> Unit): T? {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                limit(1)
                where(filter)
            }
            return executeQuery(action) { rs ->
                if (rs.next()) linkHandler!!.readJoinResult<T>(typeClass, rs) else null
            }
        }
        val action = ActionSelect(table.name).apply {
            limit(1)
            where(filter)
        }
        val map = executeQuery(action) { rs -> readMap(typeClass, rs) }
        return resolveOneWithCollections(typeClass, map)
    }

    override fun <T> get(type: Class<T>, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where(filter)
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val action = ActionSelect(table.name).apply {
            where(filter)
        }
        val maps = executeQuery(action) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> findOne(type: Class<T>, id: Any, filter: Filter.() -> Unit): T? {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val primaryName = typeClass.primaryMemberName ?: error("No primary id found.")
            val tableName = table.name
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where("$tableName.$primaryName".asFormattedColumnName() eq id.value())
                where(filter)
                limit(1)
            }
            return executeQuery(action) { rs ->
                if (rs.next()) linkHandler!!.readJoinResult<T>(typeClass, rs) else null
            }
        }
        val map = executeQuery(selectById(typeClass, id, filter) { limit(1) }) { rs -> readMap(typeClass, rs) }
        return resolveOneWithCollections(typeClass, map)
    }

    override fun <T> find(type: Class<T>, id: Any, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val primaryName = typeClass.primaryMemberName ?: error("No primary id found.")
            val tableName = table.name
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where("$tableName.$primaryName".asFormattedColumnName() eq id.value())
                where(filter)
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val maps = executeQuery(selectById(typeClass, id, filter)) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> sort(type: Class<T>, row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where(filter)
                limit(limit)
                orderBy(row)
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val action = ActionSelect(table.name).apply {
            where(filter)
            limit(limit)
            orderBy(row)
        }
        val maps = executeQuery(action) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> sortDescending(type: Class<T>, row: String, limit: Int, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where(filter)
                limit(limit)
                orderBy(row, Order.Type.DESC)
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val action = ActionSelect(table.name).apply {
            where(filter)
            limit(limit)
            orderBy(row, Order.Type.DESC)
        }
        val maps = executeQuery(action) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> getPage(type: Class<T>, page: Int, size: Int, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where(filter)
                limit(size)
                offset((page - 1) * size)
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val action = ActionSelect(table.name).apply {
            where(filter)
            limit(size)
            offset((page - 1) * size)
        }
        val maps = executeQuery(action) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> sortPage(type: Class<T>, row: String, page: Int, size: Int, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where(filter)
                orderBy(row)
                limit(size)
                offset((page - 1) * size)
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val action = ActionSelect(table.name).apply {
            where(filter)
            orderBy(row)
            limit(size)
            offset((page - 1) * size)
        }
        val maps = executeQuery(action) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> sortDescendingPage(type: Class<T>, row: String, page: Int, size: Int, filter: Filter.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where(filter)
                orderBy(row, Order.Type.DESC)
                limit(size)
                offset((page - 1) * size)
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val action = ActionSelect(table.name).apply {
            where(filter)
            orderBy(row, Order.Type.DESC)
            limit(size)
            offset((page - 1) * size)
        }
        val maps = executeQuery(action) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> selectCursor(type: Class<T>, filter: Filter.() -> Unit): Cursor<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) { where(filter) }
            return openCursor(action) { rs -> linkHandler!!.readJoinResult<T>(typeClass, rs) }
        }
        val mapper = EntityMapper.of(type)
        val action = ActionSelect(table.name).apply { where(filter) }
        return openCursor(action) { rs -> mapper.createInstance(mapper.read(rs)) }
    }

    override fun <T> sortCursor(type: Class<T>, row: String, filter: Filter.() -> Unit): Cursor<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) { where(filter); orderBy(row) }
            return openCursor(action) { rs -> linkHandler!!.readJoinResult<T>(typeClass, rs) }
        }
        val mapper = EntityMapper.of(type)
        val action = ActionSelect(table.name).apply { where(filter); orderBy(row) }
        return openCursor(action) { rs -> mapper.createInstance(mapper.read(rs)) }
    }

    override fun <T> sortDescendingCursor(type: Class<T>, row: String, filter: Filter.() -> Unit): Cursor<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val action = linkHandler!!.buildJoinSelect(typeClass) { where(filter); orderBy(row, Order.Type.DESC) }
            return openCursor(action) { rs -> linkHandler!!.readJoinResult<T>(typeClass, rs) }
        }
        val mapper = EntityMapper.of(type)
        val action = ActionSelect(table.name).apply { where(filter); orderBy(row, Order.Type.DESC) }
        return openCursor(action) { rs -> mapper.createInstance(mapper.read(rs)) }
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
        linkHandler?.cascadeSaveLinkedObjects(typeClass, dataList)
        val columnNames = linkHandler?.getColumnNames(typeClass) ?: getColumnNamesLocal(typeClass)
        val action = ActionInsert(table.name, columnNames.toTypedArray()).apply {
            dataList.forEach { data ->
                values(linkHandler?.getColumnValues(typeClass, data) ?: getColumnValuesLocal(typeClass, data))
            }
        }
        executeUpdate(action)
        collectionHandler?.insertCollectionData(typeClass, dataList)
    }

    override fun insertAndGetKeys(dataList: List<Any>): List<Long> {
        if (dataList.isEmpty()) return emptyList()
        val typeClass = AnalyzedClass.of(dataList.first().javaClass)
        linkHandler?.cascadeSaveLinkedObjects(typeClass, dataList)
        val columnNames = linkHandler?.getColumnNames(typeClass) ?: getColumnNamesLocal(typeClass)
        val action = ActionInsert(table.name, columnNames.toTypedArray()).apply {
            dataList.forEach { data ->
                values(linkHandler?.getColumnValues(typeClass, data) ?: getColumnValuesLocal(typeClass, data))
            }
        }
        // SQLite 限制：批量插入时 generatedKeys 仅返回最后一条记录的自增 ID，
        // 而非所有记录的 ID 列表。因此在 SQLite 模式下，返回的 List 长度可能为 1 而非 dataList.size。
        // MySQL 不受此限制，会正确返回所有生成的 key。
        return withConnection { conn ->
            executePrepared(conn, action.query, action.elements, Statement.RETURN_GENERATED_KEYS) {
                executeUpdate()
                generatedKeys.use { rs ->
                    buildList { while (rs.next()) add(rs.getLong(1)) }
                }
            }
        }.also { collectionHandler?.insertCollectionData(typeClass, dataList) }
    }

    override fun update(data: Any, usePrimaryKey: Boolean, filter: Filter.() -> Unit) {
        val typeClass = AnalyzedClass.of(data::class.java)
        if (typeClass.members.none { !it.isIgnored && (!it.isFinal || it.isLinkTable) }) {
            error("No mutable field found.")
        }
        linkHandler?.cascadeSaveLinkedObjects(typeClass, listOf(data))
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
                    typeClass.members.filter { !it.isIgnored && (!it.isFinal || it.isLinkTable) && !it.isCollection }.forEach { member ->
                        if (member.isLinkTable) {
                            val linkedObj = typeClass.getValue(data, member)
                            val fkValue = if (linkedObj != null) {
                                val linkedClass = AnalyzedClass.of(member.linkTableClass!!)
                                linkedClass.getPrimaryMemberValue(linkedObj)?.value()
                            } else null
                            set(member.linkTableColumn!!, fkValue)
                        } else if (member.isFlattenedCollection) {
                            val ct = CustomTypeFactory.getCustomTypeForCollection(member.returnType, member.collectionElementType!!)!!
                            val raw = typeClass.getValue(data, member)
                            set(member.name, if (raw != null) ct.serialize(raw) else null)
                        } else {
                            set(member.name, typeClass.getValue(data, member)?.value())
                        }
                    }
                }
                executePrepared(conn, updateAction.query, updateAction.elements, Statement.RETURN_GENERATED_KEYS) { executeUpdate() }
            } else {
                val insertTypeClass = AnalyzedClass.of(data.javaClass)
                val columnNames = linkHandler?.getColumnNames(insertTypeClass) ?: getColumnNamesLocal(insertTypeClass)
                val insertAction = ActionInsert(table.name, columnNames.toTypedArray()).apply {
                    values(linkHandler?.getColumnValues(insertTypeClass, data) ?: getColumnValuesLocal(insertTypeClass, data))
                }
                executePrepared(conn, insertAction.query, insertAction.elements, Statement.RETURN_GENERATED_KEYS) { executeUpdate() }
            }
        }
        collectionHandler?.syncCollectionData(typeClass, data)
    }

    override fun updateByKey(data: Any, usePrimaryKey: Boolean) {
        val typeClass = AnalyzedClass.of(data::class.java)
        if (typeClass.members.none { !it.isIgnored && (!it.isFinal || it.isLinkTable) }) {
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
        if (typeClass.members.none { !it.isIgnored && (!it.isFinal || it.isLinkTable) }) {
            error("No mutable field found.")
        }
        linkHandler?.cascadeSaveLinkedObjects(typeClass, dataList)
        val primaryName = typeClass.primaryMemberName ?: error("No primary id found.")
        val keyMembers = typeClass.members.filter { it.isKey }
        val mutableMembers = typeClass.members.filter { !it.isIgnored && (!it.isFinal || it.isLinkTable) && !it.isCollection }
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
                        (linkHandler?.getColumnValues(typeClass, data) ?: getColumnValuesLocal(typeClass, data)).forEachIndexed { i, v ->
                            stmt.setObject(i + 1, v)
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
                            if (member.isLinkTable) {
                                val linkedObj = typeClass.getValue(data, member)
                                val fkValue = if (linkedObj != null) {
                                    val linkedClass = AnalyzedClass.of(member.linkTableClass!!)
                                    linkedClass.getPrimaryMemberValue(linkedObj)?.value()
                                } else null
                                stmt.setObject(idx++, fkValue)
                            } else if (member.isFlattenedCollection) {
                                val ct = CustomTypeFactory.getCustomTypeForCollection(member.returnType, member.collectionElementType!!)!!
                                val raw = typeClass.getValue(data, member)
                                stmt.setObject(idx++, if (raw != null) ct.serialize(raw) else null)
                            } else {
                                stmt.setObject(idx++, typeClass.getValue(data, member)?.value())
                            }
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
        dataList.forEach { data -> collectionHandler?.syncCollectionData(typeClass, data) }
    }

    override fun <T> delete(type: Class<T>, id: Any, filter: Filter.() -> Unit) {
        val typeClass = AnalyzedClass.of(type)
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        collectionHandler?.deleteCollectionDataByFk(id)
        val action = ActionDelete(table.name).apply {
            where(name eq id.value())
            where(filter)
        }
        executeUpdate(action)
    }

    override fun deleteWhere(filter: Filter.() -> Unit) {
        val action = ActionDelete(table.name).apply {
            where(filter)
        }
        executeUpdate(action)
    }

    override fun count(filter: Filter.() -> Unit): Long {
        val action = ActionSelect(table.name).apply {
            rows("COUNT(*)")
            where(filter)
        }
        return executeQuery(action) { rs -> if (rs.next()) rs.getLong(1) else 0L }
    }

    override fun <T> findByKey(type: Class<T>, data: Any, usePrimaryKey: Boolean): List<T> {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val tableName = table.name
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                if (usePrimaryKey) {
                    val pName = typeClass.primaryMemberName ?: error("No primary id found.")
                    val pValue = typeClass.getPrimaryMemberValue(data)
                    where("$tableName.$pName".asFormattedColumnName() eq pValue?.value())
                }
                typeClass.members.filter { it.isKey }.forEach { member ->
                    where("$tableName.${member.name}".asFormattedColumnName() eq typeClass.getValue(data, member)?.value())
                }
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val action = selectByKey(typeClass, data, usePrimaryKey)
        val maps = executeQuery(action) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> findOneByKey(type: Class<T>, data: Any, usePrimaryKey: Boolean): T? {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val tableName = table.name
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                if (usePrimaryKey) {
                    val pName = typeClass.primaryMemberName ?: error("No primary id found.")
                    val pValue = typeClass.getPrimaryMemberValue(data)
                    where("$tableName.$pName".asFormattedColumnName() eq pValue?.value())
                }
                typeClass.members.filter { it.isKey }.forEach { member ->
                    where("$tableName.${member.name}".asFormattedColumnName() eq typeClass.getValue(data, member)?.value())
                }
                limit(1)
            }
            return executeQuery(action) { rs ->
                if (rs.next()) linkHandler!!.readJoinResult<T>(typeClass, rs) else null
            }
        }
        val action = selectByKey(typeClass, data, usePrimaryKey) { limit(1) }
        val map = executeQuery(action) { rs -> readMap(typeClass, rs) }
        return resolveOneWithCollections(typeClass, map)
    }

    override fun <T> hasByKey(type: Class<T>, data: Any, usePrimaryKey: Boolean): Boolean {
        val typeClass = AnalyzedClass.of(type)
        val action = selectByKey(typeClass, data, usePrimaryKey) { limit(1) }
        return executeQuery(action) { it.next() }
    }

    override fun deleteByKey(data: Any, usePrimaryKey: Boolean) {
        val typeClass = AnalyzedClass.of(data::class.java)
        val action = ActionDelete(table.name).apply {
            if (usePrimaryKey) {
                val name = typeClass.primaryMemberName ?: error("No primary id found.")
                val value = typeClass.getPrimaryMemberValue(data)
                where(name eq value?.value())
            }
            typeClass.members.filter { it.isKey }.forEach { member ->
                where(member.name eq typeClass.getValue(data, member)?.value())
            }
        }
        executeUpdate(action)
    }

    override fun <T> findByRowId(type: Class<T>, rowId: Long): T? {
        val typeClass = AnalyzedClass.of(type)
        if (typeClass.hasLinkMembers) {
            val tableName = table.name
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where("$tableName.id".asFormattedColumnName() eq rowId)
                limit(1)
            }
            return executeQuery(action) { rs ->
                if (rs.next()) linkHandler!!.readJoinResult<T>(typeClass, rs) else null
            }
        }
        val action = ActionSelect(table.name).apply {
            where("id" eq rowId)
            limit(1)
        }
        val map = executeQuery(action) { rs -> readMap(typeClass, rs) }
        return resolveOneWithCollections(typeClass, map)
    }

    override fun deleteByRowId(rowId: Long) {
        val action = ActionDelete(table.name).apply {
            where("id" eq rowId)
        }
        executeUpdate(action)
    }

    // === 批量操作 ===

    override fun <T> findByIds(type: Class<T>, ids: List<Any>): List<T> {
        if (ids.isEmpty()) return emptyList()
        val typeClass = AnalyzedClass.of(type)
        val primaryName = typeClass.primaryMemberName ?: error("No primary id found.")
        if (typeClass.hasLinkMembers) {
            val tableName = table.name
            val action = linkHandler!!.buildJoinSelect(typeClass) {
                where { "$tableName.$primaryName".asFormattedColumnName() inside ids.map { it.value() }.toTypedArray() }
            }
            return executeQuery(action) { rs ->
                buildList {
                    while (rs.next()) {
                        add(linkHandler!!.readJoinResult<T>(typeClass, rs))
                    }
                }
            }
        }
        val action = ActionSelect(table.name).apply {
            where { primaryName inside ids.map { it.value() }.toTypedArray() }
        }
        val maps = executeQuery(action) { rs -> readMaps(typeClass, rs) }
        return resolveWithCollections(typeClass, maps)
    }

    override fun <T> deleteByIds(type: Class<T>, ids: List<Any>) {
        if (ids.isEmpty()) return
        val typeClass = AnalyzedClass.of(type)
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        ids.forEach { collectionHandler?.deleteCollectionDataByFk(it) }
        val action = ActionDelete(table.name).apply {
            where { name inside ids.map { it.value() }.toTypedArray() }
        }
        executeUpdate(action)
    }

    override fun updateBatch(dataList: List<Any>) {
        if (dataList.isEmpty()) return
        val typeClass = AnalyzedClass.of(dataList.first().javaClass)
        val mutableMembers = typeClass.members.filter { !it.isIgnored && (!it.isFinal || it.isLinkTable) && !it.isCollection }
        if (mutableMembers.isEmpty()) error("No mutable field found.")
        linkHandler?.cascadeSaveLinkedObjects(typeClass, dataList)
        val primaryName = typeClass.primaryMemberName ?: error("No primary id found.")
        val keyMembers = typeClass.members.filter { it.isKey }
        val updateSql = buildUpdateSql(typeClass, primaryName, keyMembers)
        withTransaction { conn ->
            conn.prepareStatement(updateSql).use { stmt ->
                dataList.forEach { data ->
                    var idx = 1
                    mutableMembers.forEach { member ->
                        if (member.isLinkTable) {
                            val linkedObj = typeClass.getValue(data, member)
                            val fkValue = if (linkedObj != null) {
                                val linkedClass = AnalyzedClass.of(member.linkTableClass!!)
                                linkedClass.getPrimaryMemberValue(linkedObj)?.value()
                            } else null
                            stmt.setObject(idx++, fkValue)
                        } else if (member.isFlattenedCollection) {
                            val ct = CustomTypeFactory.getCustomTypeForCollection(member.returnType, member.collectionElementType!!)!!
                            val raw = typeClass.getValue(data, member)
                            stmt.setObject(idx++, if (raw != null) ct.serialize(raw) else null)
                        } else {
                            stmt.setObject(idx++, typeClass.getValue(data, member)?.value())
                        }
                    }
                    stmt.setObject(idx++, typeClass.getPrimaryMemberValue(data)?.value())
                    keyMembers.forEach { member ->
                        stmt.setObject(idx++, typeClass.getValue(data, member)?.value())
                    }
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
        }
        dataList.forEach { data -> collectionHandler?.syncCollectionData(typeClass, data) }
    }

    // === 自定义 SQL ===

    override fun <R> select(action: ActionSelect, handler: (ResultSet) -> R): R {
        return executeQuery(action, handler)
    }

    override fun execute(action: Action): Int {
        return executeUpdate(action)
    }

    // === Accessor 委托方法 ===

    fun mapAccessor(id: Any, fieldName: String): DatabaseMap =
        collectionHandler!!.mapAccessor(id, fieldName, sharedConnection)

    fun mapAccessor(fieldName: String, filter: Filter.() -> Unit): DatabaseMap =
        collectionHandler!!.mapAccessor(fieldName, filter, sharedConnection)

    fun listAccessor(id: Any, fieldName: String): DatabaseList =
        collectionHandler!!.listAccessor(id, fieldName, sharedConnection)

    fun listAccessor(fieldName: String, filter: Filter.() -> Unit): DatabaseList =
        collectionHandler!!.listAccessor(fieldName, filter, sharedConnection)

    fun setAccessor(id: Any, fieldName: String): DatabaseSet =
        collectionHandler!!.setAccessor(id, fieldName, sharedConnection)

    fun setAccessor(fieldName: String, filter: Filter.() -> Unit): DatabaseSet =
        collectionHandler!!.setAccessor(fieldName, filter, sharedConnection)

    // === 私有辅助方法 ===

    /**
     * 构建 INSERT SQL
     */
    private fun buildInsertSql(typeClass: AnalyzedClass): String {
        val columnNames = linkHandler?.getColumnNames(typeClass) ?: getColumnNamesLocal(typeClass)
        val columns = columnNames.joinToString(", ") { it.asFormattedColumnName() }
        val placeholders = columnNames.joinToString(", ") { "?" }
        return "INSERT INTO ${table.name.asFormattedColumnName()} ($columns) VALUES ($placeholders)"
    }

    /**
     * 构建 UPDATE SQL
     */
    private fun buildUpdateSql(
        typeClass: AnalyzedClass,
        primaryName: String,
        keyMembers: List<AnalyzedClassMember>
    ): String {
        val mutableMembers = typeClass.members.filter { !it.isIgnored && (!it.isFinal || it.isLinkTable) && !it.isCollection }
        val setClause = mutableMembers.joinToString(", ") { member ->
            if (member.isLinkTable) "${member.linkTableColumn!!.asFormattedColumnName()} = ?" else "${member.name.asFormattedColumnName()} = ?"
        }
        val whereClause = buildString {
            append("${primaryName.asFormattedColumnName()} = ?")
            keyMembers.forEach { append(" AND ${it.name.asFormattedColumnName()} = ?") }
        }
        return "UPDATE ${table.name.asFormattedColumnName()} SET $setClause WHERE $whereClause"
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
     * 构建基于 @Id + @Key 的查询
     */
    private fun selectByKey(
        typeClass: AnalyzedClass,
        data: Any,
        usePrimaryKey: Boolean,
        extra: ActionSelect.() -> Unit = {}
    ): ActionSelect {
        return ActionSelect(table.name).apply {
            if (usePrimaryKey) {
                val name = typeClass.primaryMemberName ?: error("No primary id found.")
                val value = typeClass.getPrimaryMemberValue(data)
                where(name eq value?.value())
            }
            typeClass.members.filter { it.isKey }.forEach { member ->
                where(member.name eq typeClass.getValue(data, member)?.value())
            }
            extra()
        }
    }

    /**
     * 打开游标查询（不关闭 PreparedStatement 和 ResultSet，由 Cursor 管理生命周期）
     * 必须在事务模式下调用。
     */
    private fun <T> openCursor(action: ActionSelect, mapper: (ResultSet) -> T): Cursor<T> {
        val conn = sharedConnection ?: error("游标查询必须在事务中使用")
        val stmt = try {
            conn.prepareStatement(
                action.query,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
            ).also { s ->
                action.elements.forEachIndexed { i, v -> s.setObject(i + 1, convertParam(v)) }
            }
        } catch (ex: SQLException) {
            warning("Query: ${action.query}")
            warning("Parameters (${action.elements.size}): ${action.elements}")
            throw ex
        }
        val rs = try {
            stmt.executeQuery()
        } catch (ex: SQLException) {
            stmt.close()
            warning("Query: ${action.query}")
            warning("Parameters (${action.elements.size}): ${action.elements}")
            throw ex
        }
        return Cursor(stmt, rs, mapper)
    }

    // === 容器类型（List/Set/Map）两阶段读取辅助 ===

    /**
     * Phase 1：从 ResultSet 读取单条记录到 Map（在连接内执行）
     */
    private fun readMap(typeClass: AnalyzedClass, rs: ResultSet): MutableMap<String, Any?>? {
        return if (rs.next()) EntityMapper.of(typeClass.clazz).read(rs).toMutableMap() else null
    }

    /**
     * Phase 1：从 ResultSet 读取所有记录到 Map 列表（在连接内执行）
     */
    private fun readMaps(typeClass: AnalyzedClass, rs: ResultSet): List<MutableMap<String, Any?>> {
        val mapper = EntityMapper.of(typeClass.clazz)
        return buildList {
            while (rs.next()) {
                add(mapper.read(rs).toMutableMap())
            }
        }
    }

    /**
     * Phase 2：将单个 Map 转换为实例，并加载容器数据（连接已释放）
     */
    private fun <T> resolveOneWithCollections(typeClass: AnalyzedClass, map: MutableMap<String, Any?>?): T? {
        if (map == null) return null
        if (typeClass.hasCollectionMembers && collectionHandler != null) {
            collectionHandler!!.loadCollections(typeClass, listOf(map))
        }
        @Suppress("UNCHECKED_CAST")
        return EntityMapper.of(typeClass.clazz).createInstance(map) as T
    }

    /**
     * Phase 2：将 Map 列表转换为实例列表，并批量加载容器数据（连接已释放）
     */
    private fun <T> resolveWithCollections(typeClass: AnalyzedClass, maps: List<MutableMap<String, Any?>>): List<T> {
        if (maps.isNotEmpty() && typeClass.hasCollectionMembers && collectionHandler != null) {
            collectionHandler!!.loadCollections(typeClass, maps)
        }
        val mapper = EntityMapper.of(typeClass.clazz)
        @Suppress("UNCHECKED_CAST")
        return maps.map { mapper.createInstance(it) as T }
    }

    /**
     * 本地列名获取（无 @LinkTable 时的降级方案）
     */
    private fun getColumnNamesLocal(typeClass: AnalyzedClass): List<String> {
        return typeClass.members.filter { !it.isIgnored && !it.isCollection }.map { it.name }
    }

    /**
     * 本地列值获取（无 @LinkTable 时的降级方案）
     */
    private fun getColumnValuesLocal(typeClass: AnalyzedClass, data: Any): List<Any?> {
        return typeClass.members.filter { !it.isIgnored && !it.isCollection }.map { member ->
            if (member.isFlattenedCollection) {
                val ct = CustomTypeFactory.getCustomTypeForCollection(member.returnType, member.collectionElementType!!)!!
                val raw = typeClass.getValue(data, member)
                if (raw != null) ct.serialize(raw) else null
            } else {
                typeClass.getValue(data, member)?.value()
            }
        }
    }
}
