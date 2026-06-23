package taboolib.expansion.migration

/**
 * `_ptc_schema_history` 中的一条已执行迁移记录。
 *
 * @property version 迁移版本号
 * @property description 文件名中的描述部分
 * @property script 文件名
 * @property checksum 执行时记录的 SHA-256
 * @property appliedAt 执行完成时间戳
 * @property executionTime 执行耗时毫秒
 */
data class MigrationRecord(
    val version: Int,
    val description: String,
    val script: String,
    val checksum: String,
    val appliedAt: Long,
    val executionTime: Long,
)
