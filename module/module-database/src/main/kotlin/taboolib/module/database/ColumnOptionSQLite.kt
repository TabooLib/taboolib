package taboolib.module.database

/**
 * SQLite 数据设置
 *
 * @author sky
 * @since 2018-05-14 21:43
 */
enum class ColumnOptionSQLite(var query: String) {

    /**
     * 不能为空
     */
    NOTNULL("NOT NULL"),

    /**
     * 唯一
     */
    UNIQUE("UNIQUE"),

    /**
     * 主键
     */
    PRIMARY_KEY("PRIMARY KEY"),

    /**
     * 递增
     */
    AUTOINCREMENT("AUTOINCREMENT");
}