package io.izzel.taboolib.module.db.sqlite;

/**
 * @Author 坏黑
 * @Since 2018-12-08 13:28
 */
public enum SQLiteColumnType {

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

    public SQLiteColumn toColumn(String name) {
        return new SQLiteColumn(this, name);
    }

    public SQLiteColumn toColumn(int m, String name) {
        return new SQLiteColumn(this, m, name);
    }

    public SQLiteColumn toColumn(int m, int d, String name) {
        return new SQLiteColumn(this, m, d, name);
    }
}
