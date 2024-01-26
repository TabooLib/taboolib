package taboolib.module.database

/**
 * SQLite 数据类型
 *
 * @author 坏黑
 * @since 2018-12-08 13:28
 */
enum class ColumnTypeSQLite {

    NULL,

    /**
     * 带符号的整数，根据值的大小存储在 1、2、3、4、6 或 8 字节中。
     */
    INTEGER,

    /**
     * 浮点值，存储为 8 字节的 IEEE 浮点数字。
     */
    REAL,

    /**
     * 文本字符串，使用数据库编码（UTF-8、UTF-16BE 或 UTF-16LE）存储。
     */
    TEXT,

    /**
     * blob 数据，完全根据它的输入存储。
     */
    BLOB,

    /**
     * 数字自动转换
     */
    NUMERIC;

    operator fun invoke(name: String, parameter1: Int = 0, parameter2: Int = 0, func: ColumnSQLite.() -> Unit = {}): ColumnSQLite {
        return ColumnSQLite(this, name).also {
            it.parameter[0] = parameter1
            it.parameter[1] = parameter2
            func(it)
        }
    }
}