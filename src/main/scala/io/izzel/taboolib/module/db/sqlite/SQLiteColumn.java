package io.izzel.taboolib.module.db.sqlite;

import io.izzel.taboolib.module.db.IColumn;
import io.izzel.taboolib.util.Strings;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-14 19:09
 */
public class SQLiteColumn extends IColumn {

    public static final SQLiteColumn PRIMARY_KEY_ID = new SQLiteColumn(SQLiteColumnType.INTEGER, "id", SQLiteColumnOption.NOTNULL, SQLiteColumnOption.PRIMARY_KEY, SQLiteColumnOption.AUTOINCREMENT);

    private final SQLiteColumnType columnType;
    private int m;
    private int d;

    private final String columnName;
    private Object defaultValue;

    private SQLiteColumnOption[] columnOptions;

    public SQLiteColumn(SQLiteColumnType columnType, String columnName) {
        this(columnType, 0, 0, columnName, null);
    }

    public SQLiteColumn(SQLiteColumnType columnType, int m, String columnName) {
        this(columnType, m, 0, columnName, null);
    }

    public SQLiteColumn(SQLiteColumnType columnType, int m, int d, String columnName) {
        this(columnType, m, d, columnName, null);
    }

    public SQLiteColumn(SQLiteColumnType columnType, String columnName, SQLiteColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, null, columnOptions);
    }

    public SQLiteColumn(SQLiteColumnType columnType, String columnName, Object defaultValue) {
        this(columnType, 0, 0, columnName, defaultValue);
    }

    public SQLiteColumn(SQLiteColumnType columnType, String columnName, Object defaultValue, SQLiteColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, defaultValue, columnOptions);
    }

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
        builder.append(Arrays.stream(columnOptions).map(SQLiteColumnOption::getText).collect(Collectors.joining(" ")));
        if (defaultValue instanceof String) {
            builder.append(" DEFAULT '").append(defaultValue).append("'");
        } else if (defaultValue != null) {
            builder.append(" DEFAULT ").append(defaultValue);
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
