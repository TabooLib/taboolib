package me.skymc.taboolib.mysql.sqlite;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.mysql.IColumn;

import java.util.Arrays;

/**
 * @Author sky
 * @Since 2018-05-14 19:09
 */
public class SQLiteColumn extends IColumn {

    public static final SQLiteColumn PRIMARY_KEY_ID = new SQLiteColumn(SQLiteColumnType.INTEGER, "id", SQLiteColumnOption.NOTNULL, SQLiteColumnOption.PRIMARY_KEY, SQLiteColumnOption.AUTOINCREMENT);

    private SQLiteColumnType columnType;
    private int m;
    private int d;

    private String columnName;
    private Object defaultValue;

    private SQLiteColumnOption[] columnOptions;

    /**
     * 文本 类型常用构造器
     * new SQLColumn(SQLiteColumnType.TEXT, "username");
     */
    public SQLiteColumn(SQLiteColumnType columnType, String columnName) {
        this(columnType, 0, 0, columnName, null);
    }

    /**
     * 主键 类型常用构造器
     * new SQLColumn(SQLiteColumnType.TEXT, "username", SQLiteColumnOption.PRIMARY_KEY, SQLiteColumnOption.AUTO_INCREMENT);
     */
    public SQLiteColumn(SQLiteColumnType columnType, String columnName, SQLiteColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, null, columnOptions);
    }

    /**
     * 数据 类型常用构造器
     * new SQLColumn(SQLiteColumnType.TEXT, "player_group", "PLAYER");
     */
    public SQLiteColumn(SQLiteColumnType columnType, String columnName, Object defaultValue) {
        this(columnType, 0, 0, columnName, defaultValue);
    }

    public SQLiteColumn(SQLiteColumnType columnType, String columnName, Object defaultValue, SQLiteColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, defaultValue, columnOptions);
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
    public SQLiteColumn(SQLiteColumnType columnType, int m, int d, String columnName, Object defaultValue, SQLiteColumnOption... columnOptions) {
        this.columnType = columnType;
        this.m = m;
        this.d = d;
        this.columnName = columnName;
        this.defaultValue = defaultValue;
        this.columnOptions = columnOptions;
    }

    public SQLiteColumn m(int m) {
        this.m = m;
        return this;
    }

    public SQLiteColumn d(int d) {
        this.d = d;
        return this;
    }

    public SQLiteColumn defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public SQLiteColumn columnOptions(SQLiteColumnOption... columnOptions) {
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
        StringBuilder builder = new StringBuilder();
        Arrays.stream(columnOptions).forEach(option -> builder.append(" ").append(option.getText()));
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                builder.append(" DEFAULT '").append(defaultValue).append("'");
            } else {
                builder.append(" DEFAULT ").append(defaultValue);
            }
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "SQLiteColumn{" +
                "columnType=" + columnType +
                ", m=" + m +
                ", d=" + d +
                ", columnName='" + columnName + '\'' +
                ", defaultValue=" + defaultValue +
                ", columnOptions=" + Arrays.toString(columnOptions) +
                '}';
    }
}
