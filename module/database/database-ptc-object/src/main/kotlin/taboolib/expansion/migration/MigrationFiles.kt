package taboolib.expansion.migration

import javax.sql.DataSource

/**
 * SQL 文件迁移配置。
 * 默认扫描 classpath 下 `ptc-migrations/`，脚本命名为 `V版本__说明.sql`。
 *
 * @property path classpath 中的迁移目录
 * @property statementSeparator 同一个 SQL 文件中分隔多条语句的独占行标记
 * @property baselineOnCreate 新库自动建最新表后，是否把现有迁移脚本标记为已执行
 * @property validateChecksum 是否校验已执行脚本的 SHA-256
 * @property failOnMissingMigration 历史表中存在但资源目录缺失的脚本是否阻止启动
 * @property baselineVersion 已有老库首次接入迁移时，标记为已执行的最大版本
 * @property classLoader 读取迁移脚本资源的类加载器
 */
class MigrationFiles(
    val path: String = DEFAULT_PATH,
    val statementSeparator: String = DEFAULT_STATEMENT_SEPARATOR,
    val baselineOnCreate: Boolean = true,
    val validateChecksum: Boolean = true,
    val failOnMissingMigration: Boolean = true,
    val baselineVersion: Int? = null,
    val classLoader: ClassLoader = Thread.currentThread().contextClassLoader ?: MigrationFiles::class.java.classLoader,
) {

    /**
     * 创建当前数据源的迁移执行器。
     *
     * @param dataSource PTC 容器的数据源
     * @return 迁移执行器
     */
    fun runner(dataSource: DataSource): MigrationRunner {
        return MigrationRunner(
            dataSource = dataSource,
            files = this,
        )
    }

    companion object {

        /**
         * 默认迁移脚本目录。
         */
        const val DEFAULT_PATH = "ptc-migrations"

        /**
         * 默认 SQL 语句分段标记。
         */
        const val DEFAULT_STATEMENT_SEPARATOR = "-- @ptc:statement"
    }
}
