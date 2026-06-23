package taboolib.expansion.migration

/**
 * 版本迁移配置
 *
 * 通过 [MapperConfig.migration] 配置迁移 SQL，框架会自动跟踪版本号并按序执行。
 *
 * ```kotlin
 * val homeTable by mapper<PlayerHome>(dbFile("data.db")) {
 *     migration {
 *         version(1,
 *             "ALTER TABLE player_home ADD COLUMN x DOUBLE DEFAULT 0",
 *             "ALTER TABLE player_home ADD COLUMN y DOUBLE DEFAULT 0"
 *         )
 *         version(2,
 *             "ALTER TABLE player_home ADD COLUMN z DOUBLE DEFAULT 0"
 *         )
 *     }
 * }
 * ```
 */
class MigrationConfig {

    internal val versions = sortedMapOf<Int, List<String>>()

    /**
     * 注册一个版本的迁移 SQL
     *
     * @param version 版本号（必须大于 0，按升序执行）
     * @param sql 该版本需要执行的 SQL 语句
     */
    fun version(version: Int, vararg sql: String) {
        versions[version] = sql.toList()
    }
}
