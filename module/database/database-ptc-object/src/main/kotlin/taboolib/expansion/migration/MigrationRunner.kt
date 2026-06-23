package taboolib.expansion.migration

import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

/**
 * SQL 文件迁移执行器。
 * 负责维护 `_ptc_schema_history`，并在自动建表前执行待应用脚本。
 *
 * @property dataSource PTC 容器的数据源
 * @property files SQL 文件迁移配置
 */
class MigrationRunner(
    val dataSource: DataSource,
    val files: MigrationFiles,
) {

    /**
     * 判断当前库是否还没有业务表。
     * `_ptc_schema_history` 会先创建，因此不计入业务表。
     *
     * @return 是否为新库
     */
    fun isFreshDatabase(): Boolean {
        dataSource.connection.use { connection ->
            prepareHistoryTable(connection)
            val tableNames = mutableSetOf<String>()
            connection.metaData.getTables(connection.catalog, null, "%", arrayOf("TABLE")).use { resultSet ->
                while (resultSet.next()) {
                    tableNames += resultSet.getString("TABLE_NAME").lowercase()
                }
            }
            tableNames -= "_ptc_schema_history"
            tableNames -= "sqlite_sequence"
            return tableNames.isEmpty()
        }
    }

    /**
     * 执行所有尚未应用的 SQL 文件迁移。
     */
    fun migrate() {
        val scripts = MigrationResourceScanner(files).load()
        dataSource.connection.use { connection ->
            prepareHistoryTable(connection)
            val records = loadRecords(connection)
            validateHistory(scripts, records)
            baselineConfiguredVersion(connection, scripts, records)

            val appliedVersions = loadRecords(connection).map { it.version }.toSet()
            for (script in scripts.filter { it.version !in appliedVersions }) {
                applyScript(connection, script)
            }
        }
    }

    /**
     * 新库自动建表完成后，把当前所有脚本标记为已执行。
     */
    fun baselineLatest() {
        if (!files.baselineOnCreate) {
            return
        }
        val scripts = MigrationResourceScanner(files).load()
        if (scripts.isEmpty()) {
            return
        }
        dataSource.connection.use { connection ->
            prepareHistoryTable(connection)
            val appliedVersions = loadRecords(connection).map { it.version }.toSet()
            for (script in scripts.filter { it.version !in appliedVersions }) {
                insertRecord(
                    connection = connection,
                    script = script,
                    executionTime = 0,
                )
            }
        }
    }

    /**
     * 创建迁移历史表。
     *
     * @param connection 数据库连接
     */
    fun prepareHistoryTable(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS _ptc_schema_history (" +
                    "installed_rank INT NOT NULL PRIMARY KEY, " +
                    "version INT NOT NULL, " +
                    "description VARCHAR(255) NOT NULL, " +
                    "script VARCHAR(255) NOT NULL, " +
                    "checksum VARCHAR(64) NOT NULL, " +
                    "applied_at BIGINT NOT NULL, " +
                    "execution_time BIGINT NOT NULL)"
            )
        }
    }

    /**
     * 读取已执行迁移记录。
     *
     * @param connection 数据库连接
     * @return 迁移历史记录
     */
    fun loadRecords(connection: Connection): List<MigrationRecord> {
        connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT version, description, script, checksum, applied_at, execution_time " +
                    "FROM _ptc_schema_history ORDER BY installed_rank"
            ).use { resultSet ->
                val records = mutableListOf<MigrationRecord>()
                while (resultSet.next()) {
                    records += resultSet.toMigrationRecord()
                }
                return records
            }
        }
    }

    /**
     * 校验历史记录与当前资源目录是否一致。
     *
     * @param scripts 当前资源目录中的迁移脚本
     * @param records 历史表中的迁移记录
     */
    fun validateHistory(scripts: List<MigrationScript>, records: List<MigrationRecord>) {
        val scriptsByVersion = scripts.associateBy { it.version }
        for (record in records) {
            val script = scriptsByVersion[record.version]
            if (script == null) {
                if (files.failOnMissingMigration) {
                    throw MigrationValidationException("Applied migration is missing: V${record.version} ${record.script}")
                }
                continue
            }
            if (files.validateChecksum && record.checksum != script.checksum) {
                throw MigrationValidationException("Migration checksum changed: V${record.version} ${record.script}")
            }
        }
    }

    /**
     * 老库首次接入 SQL 文件迁移时，按配置跳过指定版本及以前的脚本。
     *
     * @param connection 数据库连接
     * @param scripts 当前资源目录中的迁移脚本
     * @param records 历史表中的迁移记录
     */
    fun baselineConfiguredVersion(connection: Connection, scripts: List<MigrationScript>, records: List<MigrationRecord>) {
        val baselineVersion = files.baselineVersion ?: return
        if (records.isNotEmpty()) {
            return
        }
        for (script in scripts.filter { it.version <= baselineVersion }) {
            insertRecord(
                connection = connection,
                script = script,
                executionTime = 0,
            )
        }
    }

    /**
     * 在事务中执行单个迁移脚本。
     * 文件内多条语句必须使用显式分段标记，避免按分号误拆 SQL 字符串或函数体。
     *
     * @param connection 数据库连接
     * @param script 待执行脚本
     */
    fun applyScript(connection: Connection, script: MigrationScript) {
        val previousAutoCommit = connection.autoCommit
        val startedAt = System.currentTimeMillis()
        connection.autoCommit = false
        try {
            connection.createStatement().use { statement ->
                for (sql in MigrationStatementReader.read(script.sql, files.statementSeparator)) {
                    statement.execute(sql)
                }
            }
            insertRecord(
                connection = connection,
                script = script,
                executionTime = System.currentTimeMillis() - startedAt,
            )
            connection.commit()
        } catch (ex: Exception) {
            connection.rollback()
            throw MigrationException("Failed to apply migration ${script.script}", ex)
        } finally {
            connection.autoCommit = previousAutoCommit
        }
    }

    /**
     * 写入迁移历史记录。
     *
     * @param connection 数据库连接
     * @param script 迁移脚本
     * @param executionTime 执行耗时毫秒
     */
    fun insertRecord(connection: Connection, script: MigrationScript, executionTime: Long) {
        connection.prepareStatement(
            "INSERT INTO _ptc_schema_history " +
                "(installed_rank, version, description, script, checksum, applied_at, execution_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
        ).use { statement ->
            statement.setInt(1, nextInstalledRank(connection))
            statement.setInt(2, script.version)
            statement.setString(3, script.description)
            statement.setString(4, script.script)
            statement.setString(5, script.checksum)
            statement.setLong(6, System.currentTimeMillis())
            statement.setLong(7, executionTime)
            statement.executeUpdate()
        }
    }

    /**
     * 获取下一条历史记录序号。
     *
     * @param connection 数据库连接
     * @return 下一条 `installed_rank`
     */
    fun nextInstalledRank(connection: Connection): Int {
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT MAX(installed_rank) FROM _ptc_schema_history").use { resultSet ->
                return if (resultSet.next()) {
                    resultSet.getInt(1) + 1
                } else {
                    1
                }
            }
        }
    }

    /**
     * 把 JDBC 结果行转换为迁移记录。
     *
     * @return 迁移历史记录
     */
    fun ResultSet.toMigrationRecord(): MigrationRecord {
        return MigrationRecord(
            version = getInt("version"),
            description = getString("description"),
            script = getString("script"),
            checksum = getString("checksum"),
            appliedAt = getLong("applied_at"),
            executionTime = getLong("execution_time"),
        )
    }
}
