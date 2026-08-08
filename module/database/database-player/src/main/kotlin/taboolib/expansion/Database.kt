package taboolib.expansion

import taboolib.common.PrimitiveIO
import taboolib.module.database.asFormattedColumnName
import taboolib.module.database.setupQuoterForHost
import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.util.IdentityHashMap
import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.sql.DataSource

class Database(val type: Type, val dataSource: DataSource = createOwnedDataSource(type)) : AutoCloseable {

    val ownsDataSource = takeOwnership(dataSource)

    private val closed = AtomicBoolean(false)

    constructor(type: Type, dataSource: DataSource, ownsDataSource: Boolean) : this(type, markOwnership(dataSource, ownsDataSource))

    private val table = type.tableVar()
    private val uniqueIndexName = createUniqueIndexName(table.name)
    private val migrationLock = migrationLocks.computeIfAbsent("${type.host().connectionUrl}|${table.name}") { Any() }

    init {
        try {
            table.createTable(dataSource)
            ensureUniqueKeyIndex()
        } catch (ex: Throwable) {
            close()
            throw ex
        }
    }

    /**
     *  根据用户获取用户所有的数据
     */
    operator fun get(user: String): MutableMap<String, String> {
        return table.select(dataSource) {
            rows("key", "value")
            where("user" eq user)
        }.map {
            getString("key") to getString("value")
        }.toMap(ConcurrentHashMap())
    }

    /**
     *  根据用户和键获取数据
     */
    operator fun get(user: String, key: String): String? {
        return table.select(dataSource) {
            rows("value")
            where("user" eq user and ("key" eq key))
            limit(1)
        }.firstOrNull {
            getString("value")
        }
    }

    /**
     *  设置用户数据
     *  如果数据为空则转为删除操作
     */
    operator fun set(user: String, key: String, data: String) {
        if (data.isEmpty()) {
            remove(user, key)
            return
        }
        when (type) {
            is TypeSQL -> table.insert(dataSource, "user", "key", "value") {
                value(user, key, data)
                onDuplicateKeyUpdate {
                    update("value", data)
                }
            }
            // SQLite 使用 ON CONFLICT DO UPDATE 原地更新，而非 INSERT OR REPLACE，
            // 后者是「删旧行再插新行」，会重置用户手工添加的额外列并推进 autoincrement。
            // 显式传入冲突字段（对应 ensureUniqueKeyIndex 建立的 user+key 唯一索引），
            // 可将 SQLite 版本门槛从 3.35.0 降到 3.24.0。
            is TypeSQLite -> table.insert(dataSource, "user", "key", "value") {
                value(user, key, data)
                onDuplicateKeyUpdate(listOf("user", "key")) {
                    update("value", data)
                }
            }
            else -> upsertGeneric(user, key, data)
        }
    }

    /**
     *  查询数据 根据 用户名 与 键
     *  如果数据不存在则返回 null
     */
    fun getValue(user: String, key: String): String? {
        return table.select(dataSource) {
            rows("key", "value")
            where("user" eq user and ("key" eq key))
        }.firstOrNull {
            getString("value")
        }
    }

    /**
     *  返回所有满足 Key = Value 的用户
     */
    fun getUserList(key: String, value: String): List<String> {
        return table.select(dataSource) {
            rows("user")
            where("key" eq key and ("value" eq value))
        }.map {
            getString("user")
        }
    }

    /**
     *  根据 Key 来返回一个 <User,Value> 的Map
     */
    fun getListByKey(key: String): MutableMap<String, String> {
        return table.select(dataSource) {
            rows("user", "value")
            where("key" eq key)
        }.map {
            getString("user") to getString("value")
        }.toMap(ConcurrentHashMap())
    }

    /**
     *  根据一个 Key 来尾缀模糊查询User的相关数据
     *  例如 key = "title-" 则会查询所有以 "title-" 开头的数据
     */
    fun getLikeKeyList(user: String, key: String): MutableMap<String, String> {
        return table.select(dataSource) {
            rows("key", "value")
            where("user" eq user and ("key" like "${key}%"))
        }.map {
            getString("key") to getString("value")
        }.toMap(ConcurrentHashMap())
    }

    /**
     *  删除符合条件的数据
     */
    fun remove(user: String, key: String) {
        table.delete(dataSource) {
            where("user" eq user and ("key" eq key))
        }
    }

    private fun upsertGeneric(user: String, key: String, data: String) {
        if (updateValue(user, key, data) > 0) {
            return
        }
        try {
            table.insert(dataSource, "user", "key", "value") {
                value(user, key, data)
            }
        } catch (ex: SQLException) {
            // 约束冲突说明该行已存在，此时 UPDATE 影响行数可能为 0（值未变化），
            // 因此以「行确实存在」而非影响行数作为成功判定，避免误抛
            if (!ex.isConstraintViolation()) {
                throw ex
            }
            updateValue(user, key, data)
            if (get(user, key) == null) {
                throw ex
            }
        }
    }

    private fun updateValue(user: String, key: String, data: String): Int {
        return table.update(dataSource) {
            set("value", data)
            where("user" eq user and ("key" eq key))
        }
    }

    private fun ensureUniqueKeyIndex() {
        synchronized(migrationLock) {
            dataSource.connection.use { connection ->
                if (findUniqueKeyIndex(connection) != null) {
                    return
                }
                if (connection.metaData.databaseProductName.orEmpty().contains("SQLite", ignoreCase = true)) {
                    migrateSQLite(connection)
                } else {
                    migrateWithRetry(connection)
                }
            }
        }
    }

    private fun migrateSQLite(connection: Connection) {
        var removedRows = 0L
        var lastFailure: SQLException? = null
        repeat(MAX_INDEX_ATTEMPTS) { attempt ->
            if (findUniqueKeyIndex(connection) != null) {
                warnDuplicateRows(removedRows)
                return
            }
            try {
                // 归并重复数据同样可能因并发迁移而失败（SQLITE_BUSY / 死锁），需与建索引一起重试
                removedRows += inTransaction(connection) {
                    val removed = removeDuplicateRows(connection)
                    createUniqueIndex(connection, resolveUniqueIndexName(connection))
                    removed
                }
            } catch (ex: SQLException) {
                if (findUniqueKeyIndex(connection) != null) {
                    warnDuplicateRows(removedRows)
                    return
                }
                lastFailure = ex
                if (attempt + 1 >= MAX_INDEX_ATTEMPTS) {
                    throw ex
                }
                return@repeat
            }
            if (findUniqueKeyIndex(connection) != null) {
                warnDuplicateRows(removedRows)
                return
            }
        }
        throw lastFailure ?: SQLException("Unable to create a unique player key index for table ${table.name}")
    }

    private fun migrateWithRetry(connection: Connection) {
        var removedRows = 0L
        var lastFailure: SQLException? = null
        repeat(MAX_INDEX_ATTEMPTS) { attempt ->
            if (findUniqueKeyIndex(connection) != null) {
                warnDuplicateRows(removedRows)
                return
            }
            // 跨节点并发迁移时，归并重复数据的大范围 DELETE 也可能撞死锁或锁等待超时，
            // 因此与创建索引共用同一套重试，避免一次失败就导致插件加载失败
            var duplicatesRemoved = false
            try {
                removedRows += inTransaction(connection) {
                    removeDuplicateRows(connection)
                }
                duplicatesRemoved = true
                createUniqueIndex(connection, resolveUniqueIndexName(connection))
            } catch (ex: SQLException) {
                if (findUniqueKeyIndex(connection) != null) {
                    warnDuplicateRows(removedRows)
                    return
                }
                lastFailure = ex
                // 重复数据已清空却仍无法建索引，说明重试无意义，直接抛出真实原因
                if (attempt + 1 >= MAX_INDEX_ATTEMPTS || (duplicatesRemoved && countDuplicateRows(connection) == 0L)) {
                    throw ex
                }
                return@repeat
            }
            if (findUniqueKeyIndex(connection) != null) {
                warnDuplicateRows(removedRows)
                return
            }
        }
        throw lastFailure ?: SQLException("Unable to create a unique player key index for table ${table.name}")
    }

    private fun createUniqueIndex(connection: Connection, indexName: String) {
        setupQuoterForHost(type.host())
        val tableName = table.name.asFormattedColumnName()
        val formattedIndexName = indexName.asFormattedColumnName()
        val userColumn = "user".asFormattedColumnName()
        val keyColumn = "key".asFormattedColumnName()
        val ifNotExists = if (connection.metaData.databaseProductName.orEmpty().contains("SQLite", ignoreCase = true)) " IF NOT EXISTS" else ""
        val query = "CREATE UNIQUE INDEX$ifNotExists $formattedIndexName ON $tableName ($userColumn, $keyColumn)"
        connection.prepareStatement(query).use { statement ->
            statement.executeUpdate()
        }
    }

    private fun findUniqueKeyIndex(connection: Connection): String? {
        return readIndices(connection).firstOrNull { index ->
            !index.nonUnique && index.columns.values.map { it.lowercase(Locale.ROOT) } == UNIQUE_KEY_COLUMNS
        }?.name
    }

    private fun resolveUniqueIndexName(connection: Connection): String {
        val existingNames = readIndices(connection).map { it.name.lowercase(Locale.ROOT) }.toHashSet()
        if (uniqueIndexName.lowercase(Locale.ROOT) !in existingNames) {
            return uniqueIndexName
        }
        for (suffix in 2..99) {
            val candidate = "${uniqueIndexName}_$suffix"
            if (candidate.lowercase(Locale.ROOT) !in existingNames) {
                return candidate
            }
        }
        throw SQLException("Unable to allocate a unique index name for table ${table.name}")
    }

    private fun readIndices(connection: Connection): List<IndexMetadata> {
        val indices = LinkedHashMap<String, IndexMetadata>()
        val tableNames = linkedSetOf(table.name, table.name.substringAfterLast('.'))
        tableNames.forEach { tableName ->
            connection.metaData.getIndexInfo(connection.catalog, null, tableName, false, false).use { result ->
                while (result.next()) {
                    val indexName = result.getString("INDEX_NAME") ?: continue
                    val columnName = result.getString("COLUMN_NAME") ?: continue
                    val index = indices.computeIfAbsent(indexName.lowercase(Locale.ROOT)) {
                        IndexMetadata(indexName, result.getBoolean("NON_UNIQUE"))
                    }
                    index.nonUnique = index.nonUnique || result.getBoolean("NON_UNIQUE")
                    index.columns[result.getShort("ORDINAL_POSITION").toInt()] = columnName
                }
            }
        }
        return indices.values.toList()
    }

    private fun removeDuplicateRows(connection: Connection): Long {
        val duplicateRows = countDuplicateRows(connection)
        if (duplicateRows == 0L) {
            return 0L
        }
        connection.prepareStatement(createDuplicateDeleteQuery(connection)).use { statement ->
            statement.executeUpdate()
        }
        val remainingRows = countDuplicateRows(connection)
        if (remainingRows > 0) {
            throw SQLException("Unable to remove duplicate player database rows from table ${table.name}")
        }
        return duplicateRows
    }

    private fun countDuplicateRows(connection: Connection): Long {
        setupQuoterForHost(type.host())
        val tableName = table.name.asFormattedColumnName()
        val userColumn = "user".asFormattedColumnName()
        val keyColumn = "key".asFormattedColumnName()
        val query = "SELECT COALESCE(SUM(group_size - 1), 0) FROM (" +
            "SELECT COUNT(*) AS group_size FROM $tableName GROUP BY $userColumn, $keyColumn HAVING COUNT(*) > 1" +
            ") duplicate_groups"
        return connection.prepareStatement(query).use { statement ->
            statement.executeQuery().use { result ->
                if (result.next()) result.getLong(1) else 0L
            }
        }
    }

    private fun createDuplicateDeleteQuery(connection: Connection): String {
        setupQuoterForHost(type.host())
        val tableName = table.name.asFormattedColumnName()
        val userColumn = "user".asFormattedColumnName()
        val keyColumn = "key".asFormattedColumnName()
        val databaseName = connection.metaData.databaseProductName.orEmpty()
        return if (databaseName.contains("SQLite", ignoreCase = true)) {
            "DELETE FROM $tableName WHERE rowid NOT IN (" +
                "SELECT MAX(rowid) FROM $tableName GROUP BY $userColumn, $keyColumn)"
        } else {
            val idColumn = "id".asFormattedColumnName()
            "DELETE FROM $tableName WHERE $idColumn NOT IN (" +
                "SELECT retained_id FROM (SELECT MAX($idColumn) AS retained_id FROM $tableName " +
                "GROUP BY $userColumn, $keyColumn) retained_rows)"
        }
    }

    private fun <T> inTransaction(connection: Connection, block: () -> T): T {
        val originalAutoCommit = connection.autoCommit
        connection.autoCommit = false
        return try {
            block().also { connection.commit() }
        } catch (ex: Throwable) {
            runCatching { connection.rollback() }.exceptionOrNull()?.let(ex::addSuppressed)
            throw ex
        } finally {
            runCatching { connection.autoCommit = originalAutoCommit }
        }
    }

    private fun warnDuplicateRows(removedRows: Long) {
        if (removedRows > 0) {
            PrimitiveIO.warning(
                "Removed {0} duplicate rows from player database table {1} before creating its unique key index.",
                removedRows,
                table.name,
            )
        }
    }

    private fun createUniqueIndexName(tableName: String): String {
        val normalizedName = tableName.replace(Regex("[^A-Za-z0-9_]"), "_").ifEmpty { "table" }
        return "uk_${normalizedName.take(36)}_${Integer.toHexString(tableName.hashCode())}_user_key"
    }

    private fun SQLException.isConstraintViolation(): Boolean {
        return this is SQLIntegrityConstraintViolationException || sqlState?.startsWith("23") == true || errorCode == 19
    }

    private data class IndexMetadata(
        val name: String,
        var nonUnique: Boolean,
        val columns: TreeMap<Int, String> = TreeMap(),
    )

    /**
     * 关闭由当前实例创建的数据源。
     *
     * 外部传入的数据源默认由调用方管理，可通过三参数构造函数显式转移所有权。
     */
    override fun close() {
        if (!closed.compareAndSet(false, true) || !ownsDataSource) {
            return
        }
        (dataSource as? AutoCloseable)?.close()
    }

    companion object {

        private const val MAX_INDEX_ATTEMPTS = 4
        private val UNIQUE_KEY_COLUMNS = listOf("user", "key")
        private val migrationLocks = ConcurrentHashMap<String, Any>()
        private val ownedDataSources = ThreadLocal.withInitial { IdentityHashMap<DataSource, Unit>() }

        private fun createOwnedDataSource(type: Type): DataSource {
            return type.host().createDataSource().also {
                ownedDataSources.get()[it] = Unit
            }
        }

        private fun markOwnership(dataSource: DataSource, ownsDataSource: Boolean): DataSource {
            val ownership = ownedDataSources.get()
            if (ownsDataSource) {
                ownership[dataSource] = Unit
            } else {
                ownership.remove(dataSource)
            }
            if (ownership.isEmpty()) {
                ownedDataSources.remove()
            }
            return dataSource
        }

        private fun takeOwnership(dataSource: DataSource): Boolean {
            val ownership = ownedDataSources.get()
            val ownsDataSource = ownership.remove(dataSource) != null
            if (ownership.isEmpty()) {
                ownedDataSources.remove()
            }
            return ownsDataSource
        }
    }
}
