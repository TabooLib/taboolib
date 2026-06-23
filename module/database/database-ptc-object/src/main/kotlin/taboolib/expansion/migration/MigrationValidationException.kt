package taboolib.expansion.migration

/**
 * SQL 文件迁移历史校验失败时抛出的异常。
 *
 * @param message 校验失败原因
 */
class MigrationValidationException(message: String) : MigrationException(message)
