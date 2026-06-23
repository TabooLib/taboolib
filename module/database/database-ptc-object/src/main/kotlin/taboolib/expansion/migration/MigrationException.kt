package taboolib.expansion.migration

/**
 * SQL 文件迁移执行失败时抛出的异常。
 *
 * @param message 失败原因
 * @param cause 原始异常
 */
open class MigrationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
