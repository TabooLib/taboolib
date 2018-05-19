package me.skymc.taboolib.mysql.builder;

import com.ilummc.tlib.util.Strings;

import java.util.Arrays;

/**
 * @Author sky
 * @Since 2018-05-14 19:09
 */
public class SQLColumn {

    /**
     * 类型
     */
    private SQLColumnType columnType;
    private int m;
    private int d;

    /**
     * 名称
     */
    private String columnName;

    /**
     * 默认值
     */
    private Object defaultValue;

    /**
     * 属性值
     */
    private SQLColumnOption[] columnOptions;

    /**
     * 文本类型常用构造器
     * new SQLColumn(SQLColumnType.TEXT, "username");
     */
    public SQLColumn(SQLColumnType columnType, String columnName) {
        this(columnType, 0, 0, columnName, null);
    }

    /**
     * 主键类型常用构造器
     * new SQLColumn(SQLColumnType.TEXT, "username", SQLColumnOption.PRIMARY_KEY, SQLColumnOption.AUTO_INCREMENT);
     */
    public SQLColumn(SQLColumnType columnType, String columnName, SQLColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, null, columnOptions);
    }

    /**
     * 数据类型常用构造器
     * new SQLColumn(SQLColumnType.TEXT, "player_group", "PLAYER");
     */
    public SQLColumn(SQLColumnType columnType, String columnName, Object defaultValue) {
        this(columnType, 0, 0, columnName, defaultValue);
    }

    /**
     * 完整构造器
     *
     * @param columnType    类型
     * @param m             m值
     * @param d             d值
     * @param columnName    名称
     * @param defaultValue  默认值
     * @param columnOptions 属性值
     */
    public SQLColumn(SQLColumnType columnType, int m, int d, String columnName, Object defaultValue, SQLColumnOption... columnOptions) {
        this.columnType = columnType;
        this.m = m;
        this.d = d;
        this.columnName = columnName;
        this.defaultValue = defaultValue;
        this.columnOptions = columnOptions;
    }

    public SQLColumn m(int m) {
        this.m = m;
        return this;
    }

    public SQLColumn d(int d) {
        this.d = d;
        return this;
    }

    public SQLColumn defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public SQLColumn columnOptions(SQLColumnOption... columnOptions) {
        this.columnOptions = columnOptions;
        return this;
    }

    public String convertToCommand() {
        if (this.m == 0 && this.d == 0) {
            return Strings.replaceWithOrder("`{0}` {1}{2}", columnName, columnType.name().toLowerCase(), convertToOptions());
        } else if (this.d == 0) {
            return Strings.replaceWithOrder("`{0}` {1}({2}){3}", columnName, columnType.name().toLowerCase(), m, convertToOptions());
        } else {
            return Strings.replaceWithOrder("`{0}` {1}({2},{3}){4}", columnName, columnType.name().toLowerCase(), m, d, convertToOptions());
        }
    }

    private String convertToOptions() {
        StringBuilder stringBuilder = new StringBuilder();
        for (SQLColumnOption options : columnOptions) {
            switch (options) {
                case NOTNULL:
                    stringBuilder.append(" not null");
                    break;
                case PRIMARY_KEY:
                    stringBuilder.append(" primary key");
                    break;
                case AUTO_INCREMENT:
                    stringBuilder.append(" auto_increment");
                    break;
                case UNIQUE_KEY:
                    stringBuilder.append(" unique key");
                    break;
                default:
            }
        }
        if (defaultValue instanceof String) {
            stringBuilder.append(" default '").append(defaultValue).append("'");
        } else {
            stringBuilder.append(" default ").append(defaultValue);
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "columnType=" + "SQLColumn{" + columnType + ", m=" + m + ", d=" + d + ", columnName='" + columnName + '\'' + ", defaultValue=" + defaultValue + ", columnOptions=" + Arrays.toString(columnOptions) + '}';
    }
}
