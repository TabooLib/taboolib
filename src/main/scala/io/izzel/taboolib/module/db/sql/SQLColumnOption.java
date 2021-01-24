package io.izzel.taboolib.module.db.sql;

/**
 * SQL 数据设置
 *
 * @author sky
 * @since 2018-05-14 21:43
 */
public enum SQLColumnOption {

    /**
     * 递增
     */
    AUTO_INCREMENT("AUTO_INCREMENT"),

    /**
     * 填充数字
     */
    ZEROFILL("ZEROFILL"),

    /**
     * 无符号（非负数）
     */
    UNSIGNED("UNSIGNED"),

    /**
     * 非空
     */
    NOTNULL("NOT NULL"),

    /**
     * 主键
     */
    PRIMARY_KEY("PRIMARY KEY"),

    /**
     * 唯一索引
     */
    UNIQUE_KEY("UNIQUE KEY"),

    /**
     * 普通索引
     */
    KEY("KEY"),

    ;

    String text;

    SQLColumnOption() {
    }

    SQLColumnOption(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
