package io.izzel.taboolib.module.db.sqlite;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.IColumn;
import io.izzel.taboolib.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQLite 数据列
 *
 * @author sky
 * @since 2018-05-14 19:09
 */
public class SQLiteColumn extends IColumn {

    public static final SQLiteColumn PRIMARY_KEY_ID = new SQLiteColumn(SQLiteColumnType.INTEGER, "id")
            .columnOptions(
                    SQLiteColumnOption.NOTNULL,
                    SQLiteColumnOption.AUTOINCREMENT,
                    SQLiteColumnOption.PRIMARY_KEY
            );

    private final SQLiteColumnType columnType;
    private int m;
    private int d;

    private final String columnName;
    private Object defaultValue;

    private final List<SQLiteColumnOption> columnOptions = Lists.newArrayList();

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
        this.columnOptions.addAll(Arrays.asList(columnOptions));
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
        this.columnOptions.addAll(Arrays.asList(columnOptions));
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
        builder.append(columnOptions.stream().map(SQLiteColumnOption::getText).collect(Collectors.joining(" ")));
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
                ", columnOptions=" + columnOptions +
                '}';
    }

    public SQLiteColumnType getColumnType() {
        return columnType;
    }

    public int getM() {
        return m;
    }

    public int getD() {
        return d;
    }

    public String getColumnName() {
        return columnName;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public List<SQLiteColumnOption> getColumnOptions() {
        return columnOptions;
    }
}
