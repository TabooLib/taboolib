package me.skymc.taboolib.mysql.builder;

/**
 * @Author sky
 * @Since 2018-05-14 21:43
 */
public enum SQLColumnOption {

    /**
     * 不能为空
     */
    NOTNULL,

    /**
     * 唯一
     */
    UNIQUE_KEY,

    /**
     * 主键
     */
    PRIMARY_KEY,

    /**
     * 递增
     */
    AUTO_INCREMENT

}
