package io.izzel.taboolib.module.db.sqlite;

/**
 * SQLite 数据设置
 *
 * @Author sky
 * @Since 2018-05-14 21:43
 */
public enum SQLiteColumnOption {

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

    String text;

    SQLiteColumnOption(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
