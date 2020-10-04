package io.izzel.taboolib.module.db.sql;

import io.izzel.taboolib.module.db.IColumn;
import io.izzel.taboolib.util.Strings;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-14 19:09
 */
public class SQLColumn extends IColumn {

    public static final SQLColumn PRIMARY_KEY_ID = new SQLColumn(SQLColumnType.BIGINT, "id")
            .columnOptions(
                    SQLColumnOption.PRIMARY_KEY,
                    SQLColumnOption.NOTNULL,
                    SQLColumnOption.AUTO_INCREMENT
            );

    private final SQLColumnType columnType;
    private int m;
    private int d;

    private final String columnName;
    private Object defaultValue;

    private SQLColumnOption[] columnOptions;

    public SQLColumn(SQLColumnType columnType, String columnName) {
        this(columnType, 0, 0, columnName, null);
    }

    public SQLColumn(SQLColumnType columnType, int m, String columnName) {
        this(columnType, m, 0, columnName, null);
    }

    public SQLColumn(SQLColumnType columnType, int m, int d, String columnName) {
        this(columnType, m, d, columnName, null);
    }

    public SQLColumn(SQLColumnType columnType, String columnName, SQLColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, null, columnOptions);
    }

    public SQLColumn(SQLColumnType columnType, String columnName, Object defaultValue) {
        this(columnType, 0, 0, columnName, defaultValue);
    }

    public SQLColumn(SQLColumnType columnType, String columnName, Object defaultValue, SQLColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, defaultValue, columnOptions);
    }

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

    @Override
    public String convertToCommand() {
        if (this.m == 0 && this.d == 0) {
            return Strings.replaceWithOrder("`{0}` {1} {2}", columnName, columnType.name().toLowerCase(), convertToOptions());
        } else if (this.d == 0) {
            return Strings.replaceWithOrder("`{0}` {1}({2}) {3}", columnName, columnType.name().toLowerCase(), m, convertToOptions());
        } else {
            return Strings.replaceWithOrder("`{0}` {1}({2},{3}) {4}", columnName, columnType.name().toLowerCase(), m, d, convertToOptions());
        }
    }

    private String convertToOptions() {
        StringBuilder builder = new StringBuilder();
        builder.append(Arrays.stream(columnOptions).map(SQLColumnOption::getText).collect(Collectors.joining(" ")));
        if (defaultValue instanceof String) {
            builder.append(" DEFAULT '").append(defaultValue).append("'");
        } else if (defaultValue != null) {
            builder.append(" DEFAULT ").append(defaultValue);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "SQLColumn{" +
                "columnType=" + columnType +
                ", m=" + m +
                ", d=" + d +
                ", columnName='" + columnName + '\'' +
                ", defaultValue=" + defaultValue +
                ", columnOptions=" + Arrays.toString(columnOptions) +
                '}';
    }
}
