package io.izzel.taboolib.module.mysql.builder;

/**
 * @Author sky
 * @Since 2018-05-14 21:43
 */
public enum SQLColumnOption {

    /**
     * 不能为空
     */
    NOTNULL("NOT NULL"),

    /**
     * 唯一
     */
    UNIQUE_KEY("UNIQUE KEY"),

    /**
     * 主键
     */
    PRIMARY_KEY("PRIMARY KEY"),

    /**
     * 递增
     */
    AUTO_INCREMENT("AUTO_INCREMENT");

    String text;

    SQLColumnOption(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
